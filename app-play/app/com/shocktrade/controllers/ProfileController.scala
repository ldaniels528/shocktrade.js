package com.shocktrade.controllers

import com.shocktrade.models.profile.{UserProfile, UserProfileDAO, UserProfiles}
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
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, BSONObjectID}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * User Profile REST Controller
 * @author lawrence.daniels@gmail.com
 */
object ProfileController extends Controller with MongoController with ErrorHandler {
  lazy val mcU = db.collection[BSONCollection]("PlayerUpdates")

  ////////////////////////////////////////////////////////////////////////////
  //      API functions
  ////////////////////////////////////////////////////////////////////////////

  def createProfile = Action.async { request =>
    // attempt to retrieve the account info properties from the request
    Try(request.body.asJson map (_.as[ProfileForm])) match {
      case Success(Some(form)) =>
        val profile = UserProfile(name = form.userName, facebookID = form.facebookID, email = form.email)
        UserProfiles.createProfile(profile) map { _ => Ok(Json.toJson(profile)) } recover {
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

  def deductFunds(id: String) = Action.async { implicit request =>
    request.body.asJson flatMap (_.asOpt[WalletForm]) match {
      case Some(form) =>
        UserProfiles.deductFunds(id.toBSID, form.adjustment) map {
          case Some(profile) => Ok(Json.toJson(profile))
          case None => BadRequest("wallet adjustment failed")
        }
      case _ =>
        Future.successful(BadRequest("JSON object expected"))
    }
  }

  /**
   * Returns a trading clock state object
   */
  def findProfileByFacebookID(id: String) = Action.async {
    UserProfiles.findProfileByFacebookID(id) map {
      case Some(profile) => Ok(Json.toJson(profile))
      case None => Ok(createError("No profile found"))
    } recover {
      case e: Exception =>
        e.printStackTrace()
        Ok(createError(e))
    }
  }

  def findFacebookFriends = Action.async { request =>
    request.body.asJson match {
      case Some(JsArray(values)) =>
        UserProfiles.findFacebookFriends(values.map(_.as[String])) map (o => Ok(Json.toJson(o)))
      case _ =>
        Future.successful(BadRequest("JSON array of IDs expected"))
    }
  }

  /**
   * Adds a favorite symbol to the specified profile
   * REST: PUT /api/profile/:id/favorite/:symbol
   */
  def addFavoriteSymbol(id: String, symbol: String) = Action.async {
    UserProfileDAO.addFavoriteSymbol(id, symbol) map (outcome => Ok(JS("symbol" -> symbol, "error" -> outcome.errmsg)))
  }

  /**
   * Removes a favorite symbol from the specified profile
   * REST: DELETE /api/profile/:id/favorite/:symbol
   */
  def removeFavoriteSymbol(id: String, symbol: String) = Action.async {
    UserProfileDAO.removeFavoriteSymbol(id, symbol) map (outcome => Ok(JS("symbol" -> symbol, "error" -> outcome.errmsg)))
  }

  /**
   * Adds a recently viewed symbol to the specified profile
   * REST: PUT /api/profile/:id/recent/:symbol
   */
  def addRecentSymbol(id: String, symbol: String) = Action.async {
    UserProfileDAO.addRecentSymbol(id, symbol) map (outcome => Ok(JS("symbol" -> symbol, "error" -> outcome.errmsg)))
  }

  /**
   * Removes a recently viewed symbol
   * REST: DELETE /api/profile/:id/recent/:symbol
   */
  def removeRecentSymbol(id: String, symbol: String) = Action.async {
    UserProfileDAO.removeRecentSymbol(id, symbol) map (outcome => Ok(JS("symbol" -> symbol, "error" -> outcome.errmsg)))
  }

  /**
   * Deletes a collection of notifications
   * REST: DELETE /api/updates
   */
  def deleteNotifications = Action.async { request =>
    request.body.asText match {
      case Some(msg) if msg.startsWith("[") && msg.endsWith("]") =>
        val messageIDs = msg.drop(1).dropRight(1).split(",").map(s => s.drop(1).dropRight(1)).toSeq
        val task = Future.sequence(messageIDs map (id => mcU.remove(BS("_id" -> BSONObjectID(id)))))
        task map (r => Ok(""))
      case _ =>
        System.out.println(s"request = ${request.body.asText}")
        Future.successful(BadRequest("JSON array of IDs expected"))
    }
  }

  def getNotifications(userName: String, limit: Int) = Action.async {
    mcU.find(BS("userName" -> userName)).cursor[BS]().collect[Seq](limit) map (o => Ok(Json.toJson(o)))
  }

  def toExchangeUpdate(js: JsValue): ExchangeUpdate = {
    val id = (js \ "id").asOpt[String].getOrElse(throw new FieldException("id"))
    val exchanges = (js \ "exchanges").asOpt[Array[String]].getOrElse(throw new FieldException("exchanges"))
    ExchangeUpdate(id, exchanges)
  }

  class FieldException(val field: String) extends RuntimeException(s"Required field '$field' is missing")

  case class ProfileForm(userName: String, facebookID: String, email: Option[String])

  implicit val profileFormReads: Reads[ProfileForm] = (
    (__ \ "userName").read[String] and
      (__ \ "facebookID").read[String] and
      (__ \ "email").readNullable[String])(ProfileForm.apply _)

  case class ExchangeUpdate(id: String, exchanges: Seq[String])

  case class WalletForm(authCode: String, adjustment: BigDecimal)

  implicit val walletFormReads: Reads[WalletForm] = (
    (__ \ "authCode").read[String] and
      (__ \ "adjustment").read[BigDecimal])(WalletForm.apply _)

}