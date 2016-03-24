package com.shocktrade.dao

import com.shocktrade.models.profile.AwardCodes.AwardCode
import com.shocktrade.models.profile.UserProfile
import com.shocktrade.util.BSONHelper._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONArray, BSONObjectID, BSONDocument => BS, _}
import reactivemongo.core.commands.LastError

import scala.concurrent.{ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}

/**
  * User Profiles DAO
  * @author lawrence.daniels@gmail.com
  */
case class UserProfileDAO(reactiveMongoApi: ReactiveMongoApi) {
  private val EXCHANGES = Seq("AMEX", "NASDAQ", "NYSE", "OTCBB", "OTHER_OTC")
  private val db = reactiveMongoApi.db
  private val mcP = db.collection[BSONCollection]("Players")
  private val mcU = db.collection[BSONCollection]("PlayerUpdates")

  /**
    * Applies the given set of awards to the specified user profile
    * @param userID the given user ID
    * @param awards the given collection of awards
    */
  def applyAwards(userID: BSONObjectID, awards: Seq[AwardCode])(implicit ec: ExecutionContext) = {
    mcP.update(BS("_id" -> userID), BS("$addToSet" -> BS("awards" -> BS("$each" -> awards.map(_.toString)))), upsert = false, multi = false)
  }

  /**
    * Creates the given user profile
    * @param profile the given user profile
    * @return a promise of the [[LastError outcome]]
    */
  def createProfile(profile: UserProfile)(implicit ec: ExecutionContext) = {
    mcP.insert(profile)
  }

  /**
    * Retrieves a user profile by the user's name
    * @param userID         the given user ID
    * @param amountToDeduct the amount to deduct
    * @return a promise of an option of a user profile
    */
  def deductFunds(userID: BSONObjectID, amountToDeduct: BigDecimal)(implicit ec: ExecutionContext) = {
    mcP.findAndUpdate(
      selector = BS("_id" -> userID /*, "networth" -> BS("$gte" -> amountToDeduct)*/),
      update = BS("$inc" -> BS("netWorth" -> -amountToDeduct)),
      fetchNewObject = true, upsert = false
    ) map (_.result[UserProfile])
  }

  def delete(id: String)(implicit ec: ExecutionContext) = {
    mcU.remove(BS("_id" -> BSONObjectID(id)))
  }

  def findExchanges(userID: String)(implicit ec: ExecutionContext) = {
    mcP.find(BS("_id" -> userID.toBSID), BS("exchanges" -> 1)).cursor[BS]().collect[Seq]()
  }

  def setExchanges(userID: String, exchange: Seq[String])(implicit ec: ExecutionContext) = {
    // db.Players.update({_id:"51a308ac50c70a97d375a6b2", {$set:{exchangesToExclude:["AMEX", "NASDAQ", "NYSE"]}})
    mcP.update(
      BS("_id" -> userID.toBSID),
      BS("$set" -> BS("exchanges" -> BSONArray(exchange))),
      upsert = false, multi = false)
  }

  /**
    * Retrieves the exchanges for the given player
    * @param playerID the user ID that represents the player
    * @return a [[Future]] of a sequence of exchanges (strings)
    */
  def findStockExchanges(playerID: Option[String])(implicit ec: ExecutionContext): Future[Seq[String]] = {
    playerID match {
      case None => Future.successful(EXCHANGES)
      case Some(userID) =>
        for {
        // attempt to retrieve the user's profile
          profile_? <- mcP.find(BS("_id" -> BSONObjectID(userID)), BS("exchanges" -> 1))
            .cursor[BS]()
            .headOption

          // attempt to extract the exchanges
          exchanges = (for {
            profile <- profile_?
            exchanges <- profile.getAs[Seq[String]]("exchanges")
          } yield exchanges) getOrElse Nil
        } yield exchanges
    }
  }


  def findFacebookFriends(fbIds: Seq[String])(implicit ec: ExecutionContext) = {
    // db.Players.find({facebookID:{$in:["100001920054300", "100001992439064"]}}, {name:1})
    mcP.find(BS("facebookID" -> BS("$in" -> fbIds)), BS("name" -> 1, "facebookID" -> 1)).cursor[BS]().collect[Seq]()
  }

  /**
    * Retrieves a user profile by the user's name
    * @param name the given user name (e.g. "ldaniels528")
    * @return a promise of an option of a user profile
    */
  def findProfileByName(name: String)(implicit ec: ExecutionContext): Future[Option[UserProfile]] = {
    mcP.find(BS("name" -> name)).one[UserProfile]
  }

  /**
    * Retrieves a user profile by the user's Facebook ID
    * @param fbId the given user's Facebook ID
    * @return a promise of an option of a user profile
    */
  def findProfileByFacebookID(fbId: String)(implicit ec: ExecutionContext): Future[Option[UserProfile]] = {
    mcP.find(BS("facebookID" -> fbId)).one[UserProfile]
  }

  def findNotifications(userName: String, limit: Int)(implicit ec: ExecutionContext) = {
    mcU.find(BS("userName" -> userName)).cursor[BS]().collect[Seq](limit)
  }

  /**
    * Adds a favorite symbol to the specified profile
    * REST: PUT /api/profile/:id/favorite/:symbol
    */
  def addFavoriteSymbol(id: String, symbol: String)(implicit ec: ExecutionContext) = {
    // db.Players.update({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {$addToSet:{"favorites" : "AHFD"}});
    mcP.update(BS("_id" -> BSONObjectID(id)), BS("$addToSet" -> BS("favorites" -> symbol)), upsert = false, multi = false)
  }

  /**
    * Removes a favorite symbol from the specified profile
    * REST: DELETE /api/profile/:id/favorite/:symbol
    */
  def removeFavoriteSymbol(id: String, symbol: String)(implicit ec: ExecutionContext) = {
    // db.Players.update({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {$pull:{"favorites" : "AHFD"}});
    // db.Players.find({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {favorites:1});
    mcP.update(BS("_id" -> BSONObjectID(id)), BS("$pull" -> BS("favorites" -> symbol)), upsert = false, multi = false)
  }

  /**
    * Adds a recently viewed symbol to the specified profile
    * REST: PUT /api/profile/:id/recent/:symbol
    */
  def addRecentSymbol(id: String, symbol: String)(implicit ec: ExecutionContext) = {
    // db.Players.update({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {$addToSet:{"favorites" : "AHFD"}});
    mcP.update(BS("_id" -> BSONObjectID(id)), BS("$addToSet" -> BS("recentSymbols" -> symbol)), upsert = false, multi = false)
  }

  /**
    * Removes a recently viewed symbol
    * REST: DELETE /api/profile/:id/recent/:symbol
    */
  def removeRecentSymbol(id: String, symbol: String)(implicit ec: ExecutionContext) = {
    mcP.update(BS("_id" -> BSONObjectID(id)), BS("$pull" -> BS("recentSymbols" -> symbol)), upsert = false, multi = false)
  }

}
