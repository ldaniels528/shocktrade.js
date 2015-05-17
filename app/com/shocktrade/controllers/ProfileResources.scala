package com.shocktrade.controllers

import com.shocktrade.models.profile.{UserProfile, UserProfiles}
import com.shocktrade.util.BSONHelper._
import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.GetLastError

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * User Profile Resources
 * @author lawrence.daniels@gmail.com
 */
object ProfileResources extends Controller with MongoController with ErrorHandler {
  lazy val mcP = db.collection[JSONCollection]("Players")
  lazy val mcU = db.collection[JSONCollection]("PlayerUpdates")

  ////////////////////////////////////////////////////////////////////////////
  //      API functions
  ////////////////////////////////////////////////////////////////////////////

  def createProfile = Action.async { request =>
    // attempt to retrieve the account info properties from the request
    Try(request.body.asJson map (_.as[ProfileForm])) match {
      case Success(Some(form)) =>
        val profile = UserProfile(name = form.userName, facebookID = form.facebookID, email = form.email)
        val profileJs = Json.toJson(profile)
        mcP.insert(profileJs) map { _ => Ok(profileJs) } recover {
          case e: Exception =>
            Logger.error(s"Error creating user profile [${request.body.asJson.orNull}]", e)
            val messages = e.getMessage match {
              case s if s.contains("duplicate key") => "Screen name is already taken"
              case _ => "The service is temporarily unavailable"
            }
            Ok(JS("status" -> "error", "error" -> messages))
        }
      case Success(None) =>
        Logger.error(s"Account information was incomplete: json => ${request.body.asJson}")
        Future.successful(BadRequest("Account information was incomplete"))
      case Failure(e) =>
        Logger.error(s"${e.getMessage}: json => ${request.body.asJson}")
        Future.successful(BadRequest(e.getMessage))
    }
  }

  def getExchanges(id: String) = Action.async { implicit request =>
    mcP.find(JS("_id" -> BSONObjectID(id)), JS("exchanges" -> 1))
      .cursor[JsObject]
      .collect[Seq]() map (o => Ok(JsArray(o)))
  }

  def setExchanges() = Action.async { implicit request =>
    request.body.asJson match {
      case Some(js) =>
        val params = toExchangeUpdate(js)

        // db.Players.update({_id:"51a308ac50c70a97d375a6b2", {$set:{exchangesToExclude:["AMEX", "NASDAQ", "NYSE"]}})
        mcP.update(
          JS("_id" -> BSONObjectID(params.id)),
          JS("$set" -> JS("exchanges" -> JsArray(params.exchanges map (Json.toJson(_))))),
          new GetLastError(),
          upsert = false, multi = false) map (r => Ok(r.errMsg getOrElse ""))
      case _ =>
        Future.successful(BadRequest("JSON object expected"))
    }
  }

  /**
   * Returns a trading clock state object
   */
  def findProfileByFacebookID(id: String) = Action.async {
    UserProfiles.findProfileByFacebookID(id) map {
      case Some(profile) =>
        Ok(Json.toJson(profile))
      case None => Ok(createError("No profile found"))
    } recover {
      case e: Exception =>
        e.printStackTrace()
        Ok(createError(e))
    }
  }

  def findFacebookFriends = Action.async { request =>
    request.body.asJson match {
      case Some(fbIds: JsArray) =>
        // db.Players.find({facebookID:{$in:["100001920054300", "100001992439064"]}}, {name:1})
        mcP.find(JS("facebookID" -> JS("$in" -> fbIds)), JS("name" -> 1, "facebookID" -> 1))
          .cursor[JsObject]
          .collect[Seq]() map (o => Ok(JsArray(o)))
      case _ =>
        Future.successful(BadRequest("JSON array of IDs expected"))
    }
  }

  /**
   * Adds a favorite symbol to the specified profile
   * REST: PUT /api/profile/:id/favorite/:symbol
   */
  def addFavoriteSymbol(id: String, symbol: String) = Action.async {
    for {
    // db.Players.update({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {$addToSet:{"favorites" : "AHFD"}});
      response <- mcP.update(JS("_id" -> BSONObjectID(id)), JS("$addToSet" -> JS("favorites" -> symbol)),
        new GetLastError(), upsert = false, multi = false)
    } yield Ok(symbol)
  }

  /**
   * Removes a favorite symbol from the specified profile
   * REST: DELETE /api/profile/:id/favorite/:symbol
   */
  def removeFavoriteSymbol(id: String, symbol: String) = Action.async {
    for {
    // db.Players.update({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {$pull:{"favorites" : "AHFD"}});
    // db.Players.find({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {favorites:1});
      response <- mcP.update(JS("_id" -> BSONObjectID(id)), JS("$pull" -> JS("favorites" -> symbol)),
        new GetLastError(), upsert = false, multi = false)
    } yield Ok(symbol)
  }

  /**
   * Adds a recently viewed symbol to the specified profile
   * REST: PUT /api/profile/:id/recent/:symbol
   */
  def addRecentSymbol(id: String, symbol: String) = Action.async {
    for {
    // db.Players.update({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {$addToSet:{"favorites" : "AHFD"}});
      response <- mcP.update(JS("_id" -> BSONObjectID(id)), JS("$addToSet" -> JS("recentSymbols" -> symbol)),
        new GetLastError(), upsert = false, multi = false)
    } yield Ok(symbol)
  }

  /**
   * Removes a recently viewed symbol
   * REST: DELETE /api/profile/:id/recent/:symbol
   */
  def removeRecentSymbol(id: String, symbol: String) = Action.async {
    for {
      response <- mcP.update(JS("_id" -> BSONObjectID(id)), JS("$pull" -> JS("recentSymbols" -> symbol)),
        new GetLastError(), upsert = false, multi = false)
    } yield Ok(symbol)
  }

  /**
   * Deletes a collection of notifications
   * REST: DELETE /api/updates
   */
  def deleteNotifications() = Action.async { request =>
    request.body.asText match {
      case Some(msg) if msg.startsWith("[") && msg.endsWith("]") =>
        val messageIDs = msg.drop(1).dropRight(1).split(",").map(s => s.drop(1).dropRight(1)).toSeq
        val task = Future.sequence(messageIDs map (id => mcU.remove(JS("_id" -> BSONObjectID(id)))))
        task map (r => Ok(""))
      case _ =>
        System.out.println(s"request = ${request.body.asText}")
        Future.successful(BadRequest("JSON array of IDs expected"))
    }
  }

  def getNotifications(userName: String, limit: Int) = Action.async {
    mcU.find(JS("userName" -> userName)).cursor[JsObject].collect[Seq](limit) map (o => Ok(JsArray(o)))
  }

  /**
   * Facilitates the purchase of perks
   * Returns the updated perks (e.g. ['CREATOR', 'PRCHEMNT'])
   */
  def purchasePerks(userId: String) = Action.async { request =>
    // get the perks from the request body
    request.body.asJson map (_.as[Seq[String]]) match {
      case Some(perkCodes) =>
        // create the perk code to cost mapping
        val perkCodeCostMapping = Map(UserProfiles.findAllPerks map (p => (p.code, p.cost)): _*)

        // compute the total cost of the perks
        val totalCost = (perkCodes flatMap perkCodeCostMapping.get).sum

        // find and modify the profile with the perks
        UserProfiles.purchasePerks(userId.toBSID, perkCodes, totalCost) map {
          case Some(profile) =>
            val js = Json.toJson(profile)
            Ok(JS("perks" -> (js \ "perks")) ++ JS("netWorth" -> (js \ "netWorth")))
          case None =>
            Ok(JS("error" -> "Perks could not be purchased"))
        } recover {
          case e => Ok(JS("error" -> "Perks could not be purchased"))
        }
      case _ =>
        Future.successful(BadRequest("JSON array of Perk codes expected"))
    }
  }

  def getAllPerks = Action(Ok(Json.toJson(UserProfiles.findAllPerks)))

  case class ProfileForm(userName: String,
                         facebookID: String,
                         email: Option[String])

  implicit val profileFormReads: Reads[ProfileForm] = (
    (__ \ "userName").read[String] and
      (__ \ "facebookID").read[String] and
      (__ \ "email").readNullable[String])(ProfileForm.apply _)

  class FieldException(val field: String) extends RuntimeException(s"Required field '$field' is missing")

  def toExchangeUpdate(js: JsValue): ExchangeUpdate = {
    val id = (js \ "id").asOpt[String].getOrElse(throw new FieldException("id"))
    val exchanges = (js \ "exchanges").asOpt[Array[String]].getOrElse(throw new FieldException("exchanges"))
    ExchangeUpdate(id, exchanges)
  }

  case class ExchangeUpdate(id: String, exchanges: Seq[String])

}