package com.shocktrade.server.dao
package users

import io.scalajs.npm.mongodb._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Profile DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait ProfileDAO extends Collection

/**
  * Profile DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ProfileDAO {

  /**
    * Profile DAO Enrichment
    * @param dao the given [[ProfileDAO profile DAO]]
    */
  implicit class ProfileDAOEnrichment(val dao: ProfileDAO) extends AnyVal {

    @inline
    def deductFunds(userID: String, amount: Double)(implicit ec: ExecutionContext): js.Promise[UpdateWriteOpResultObject] = {
      dao.updateOne(filter = doc("_id" $eq userID.$oid, "wallet" $gte amount), update = "wallet" $inc -amount)
    }

    @inline
    def depositFunds(userID: String, amount: Double)(implicit ec: ExecutionContext): js.Promise[UpdateWriteOpResultObject] = {
      dao.updateOne(filter = "_id" $eq userID.$oid, update = "wallet" $inc amount)
    }

    @inline
    def findOneByID(userID: String)(implicit ec: ExecutionContext): Future[Option[UserProfileData]] = {
      dao.findOneFuture[UserProfileData]("_id" $eq userID.$oid)
    }

    @inline
    def findOneByFacebookID(fbId: String)(implicit ec: ExecutionContext): Future[Option[UserProfileData]] = {
      dao.findOneFuture[UserProfileData]("facebookID" $eq fbId)
    }

    @inline
    def findOneOrCreateByFacebook(fbProfile: UserProfileData, fbId: String)(implicit ec: ExecutionContext): js.Promise[FindAndModifyWriteOpResult] = {
      fbProfile._id = new ObjectID()
      dao.findOneAndUpdate(filter = "facebookID" $eq fbId, update = doc(
        "$setOnInsert" -> fbProfile // ,
        //"lastLoginTime" $set new js.Date()
      ), new FindAndUpdateOptions(upsert = true, returnOriginal = false))
    }

    @inline
    def addFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(filter = "_id" $eq userID.$oid, update = "favoriteSymbols" $addToSet symbol)
    }

    @inline
    def addRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(filter = "_id" $eq userID.$oid, update = "recentSymbols" $addToSet symbol)
    }

    @inline
    def removeFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(filter = "_id" $eq userID.$oid, update = "favoriteSymbols" $pull symbol)
    }

    @inline
    def removeRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(filter = "_id" $eq userID.$oid, update = "recentSymbols" $pull symbol)
    }

    @inline
    def updateNetWorth(userID: String, netWorth: Double)(implicit ec: ExecutionContext): js.Promise[UpdateWriteOpResultObject] = {
      dao.updateOne(filter = "_id" $eq userID.$oid, update = "netWorth" $set netWorth)
    }

  }

  /**
    * Profile DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class ProfileDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getProfileDAO(implicit ec: ExecutionContext): ProfileDAO = {
      db.collection("Players").asInstanceOf[ProfileDAO]
    }
  }

}
