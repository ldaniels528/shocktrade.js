package com.shocktrade.models.profile

import com.shocktrade.controllers.ProfileController._
import com.shocktrade.core.AwardCodes.AwardCode
import com.shocktrade.util.BSONHelper._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONArray, BSONDocument => BS, BSONObjectID, _}
import reactivemongo.core.commands.{FindAndModify, LastError, Update}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}

/**
 * User Profiles DAO
 * @author lawrence.daniels@gmail.com
 */
object UserProfileDAO {
  private implicit val mc = db.collection[BSONCollection]("Players")

  /**
   * Applies the given set of awards to the specified user profile
   * @param userID the given user ID
   * @param awards the given collection of awards
   */
  def applyAwards(userID: BSONObjectID, awards: Seq[AwardCode])(implicit ec: ExecutionContext): Future[LastError] = {
    mc.update(BS("_id" -> userID), BS("$addToSet" -> BS("awards" -> BS("$each" -> awards.map(_.toString)))), upsert = false, multi = false)
  }

  /**
   * Creates the given user profile
   * @param profile the given user profile
   * @return a promise of the [[LastError outcome]]
   */
  def createProfile(profile: UserProfile)(implicit ec: ExecutionContext): Future[LastError] = {
    mc.insert(profile)
  }

  /**
   * Retrieves a user profile by the user's name
   * @param userID the given user ID
   * @param amountToDeduct the amount to deduct
   * @return a promise of an option of a user profile
   */
  def deductFunds(userID: BSONObjectID, amountToDeduct: BigDecimal)(implicit ec: ExecutionContext): Future[Option[UserProfile]] = {
    val q = BS("_id" -> userID /*, "networth" -> BS("$gte" -> amountToDeduct)*/)
    val u = BS("$inc" -> BS("netWorth" -> -amountToDeduct))
    db.command(FindAndModify("Players", q, Update(u, fetchNewObject = true), upsert = false)) map (_ flatMap (_.seeAsOpt[UserProfile]))
  }

  def findExchanges(userID: String)(implicit ec: ExecutionContext): Future[Seq[BS]] = {
    mc.find(BS("_id" -> userID.toBSID), BS("exchanges" -> 1)).cursor[BS].collect[Seq]()
  }

  def setExchanges(userID: String, exchange: Seq[String])(implicit ec: ExecutionContext) = {
    // db.Players.update({_id:"51a308ac50c70a97d375a6b2", {$set:{exchangesToExclude:["AMEX", "NASDAQ", "NYSE"]}})
    mc.update(
      BS("_id" -> userID.toBSID),
      BS("$set" -> BS("exchanges" -> BSONArray(exchange))),
      upsert = false, multi = false)
  }

  def findFacebookFriends(fbIds: Seq[String])(implicit ec: ExecutionContext) = {
    // db.Players.find({facebookID:{$in:["100001920054300", "100001992439064"]}}, {name:1})
    mc.find(BS("facebookID" -> BS("$in" -> fbIds)), BS("name" -> 1, "facebookID" -> 1)).cursor[BS].collect[Seq]()
  }

  /**
   * Retrieves a user profile by the user's name
   * @param name the given user name (e.g. "ldaniels528")
   * @return a promise of an option of a user profile
   */
  def findProfileByName(name: String)(implicit ec: ExecutionContext): Future[Option[UserProfile]] = {
    mc.find(BS("name" -> name)).one[UserProfile]
  }

  /**
   * Retrieves a user profile by the user's Facebook ID
   * @param fbId the given user's Facebook ID
   * @return a promise of an option of a user profile
   */
  def findProfileByFacebookID(fbId: String)(implicit ec: ExecutionContext): Future[Option[UserProfile]] = {
    mc.find(BS("facebookID" -> fbId)).one[UserProfile]
  }

  /**
   * Adds a favorite symbol to the specified profile
   * REST: PUT /api/profile/:id/favorite/:symbol
   */
  def addFavoriteSymbol(id: String, symbol: String)(implicit ec: ExecutionContext) = {
    // db.Players.update({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {$addToSet:{"favorites" : "AHFD"}});
    mc.update(BS("_id" -> BSONObjectID(id)), BS("$addToSet" -> BS("favorites" -> symbol)), upsert = false, multi = false)
  }

  /**
   * Removes a favorite symbol from the specified profile
   * REST: DELETE /api/profile/:id/favorite/:symbol
   */
  def removeFavoriteSymbol(id: String, symbol: String)(implicit ec: ExecutionContext) = {
    // db.Players.update({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {$pull:{"favorites" : "AHFD"}});
    // db.Players.find({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {favorites:1});
    mc.update(BS("_id" -> BSONObjectID(id)), BS("$pull" -> BS("favorites" -> symbol)), upsert = false, multi = false)
  }

  /**
   * Adds a recently viewed symbol to the specified profile
   * REST: PUT /api/profile/:id/recent/:symbol
   */
  def addRecentSymbol(id: String, symbol: String)(implicit ec: ExecutionContext) = {
    // db.Players.update({"_id":ObjectId("51a308ac50c70a97d375a6b2")}, {$addToSet:{"favorites" : "AHFD"}});
    mc.update(BS("_id" -> BSONObjectID(id)), BS("$addToSet" -> BS("recentSymbols" -> symbol)), upsert = false, multi = false)
  }

  /**
   * Removes a recently viewed symbol
   * REST: DELETE /api/profile/:id/recent/:symbol
   */
  def removeRecentSymbol(id: String, symbol: String)(implicit ec: ExecutionContext) = {
    mc.update(BS("_id" -> BSONObjectID(id)), BS("$pull" -> BS("recentSymbols" -> symbol)), upsert = false, multi = false)
  }

}
