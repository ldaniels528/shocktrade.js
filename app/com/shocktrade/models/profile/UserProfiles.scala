package com.shocktrade.models.profile

import com.shocktrade.controllers.ProfileResources._
import com.shocktrade.util.BSONHelper._
import play.libs.Akka
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, BSONObjectID}
import reactivemongo.core.commands.{FindAndModify, Update}

import scala.concurrent.Future

/**
 * User Profiles Proxy
 * @author lawrence.daniels@gmail.com
 */
object UserProfiles {
  private val system = Akka.system
  private implicit val ec = system.dispatcher
  private implicit val mc = db.collection[BSONCollection]("Players")

  /**
   * Retrieves a user profile by the user's name
   * @param userID the given user ID
   * @param amountToDeduct the amount to deduct
   * @return a promise of an option of a user profile
   */
  def deductFunds(userID: BSONObjectID, amountToDeduct: BigDecimal): Future[Option[UserProfile]] = {
    val q = BS("_id" -> userID/*, "networth" -> BS("$gte" -> amountToDeduct)*/)
    val u = BS("$inc" -> BS("netWorth" -> -amountToDeduct))
    db.command(FindAndModify("Players", q, Update(u, fetchNewObject = true), upsert = false)) map (_ flatMap (_.seeAsOpt[UserProfile]))
  }

  /**
   * Retrieves all of the system-defined perks
   * @return a promise of a sequence of perks
   */
  def findAllPerks = Perk.allPerks

  /**
   * Retrieves a user profile by the user's name
   * @param name the given user name (e.g. "ldaniels528")
   * @return a promise of an option of a user profile            
   */
  def findProfileByName(name: String): Future[Option[UserProfile]] = {
    mc.find(BS("name" -> name)).one[UserProfile]
  }

  /**
   * Retrieves a user profile by the user's Facebook ID
   * @param fbId the given user's Facebook ID
   * @return a promise of an option of a user profile 
   */
  def findProfileByFacebookID(fbId: String): Future[Option[UserProfile]] = {
    mc.find(BS("facebookID" -> fbId)).one[UserProfile]
  }

  /**
   * Purchases the passed perks
   * @param userId the user the perks are being purchased by
   * @param perkCodes the given perk codes
   * @param totalCost the total cost of the perks
   * @return a promise of an option of a user profile
   */
  def purchasePerks(userId: BSONObjectID, perkCodes: Seq[String], totalCost: Double): Future[Option[UserProfile]] = {
    val q = BS("_id" -> userId, "netWorth" -> BS("$gte" -> totalCost))
    val u = BS("$addToSet" -> BS("perks" -> BS("$each" -> perkCodes)), "$inc" -> BS("netWorth" -> -totalCost))
    db.command(FindAndModify("Players", q, Update(u, fetchNewObject = true), upsert = false, sort = None)) map (_ flatMap (_.seeAsOpt[UserProfile]))
  }

}
