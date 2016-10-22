package com.shocktrade.server.dao
package users

import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
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
    def deductFunds(userID: String, amount: Double)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.updateOne(filter = doc("_id" $eq userID.$oid, "netWorth" $gte amount), update = "netWorth" $inc -amount)
    }

    @inline
    def depositFunds(userID: String, amount: Double)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.updateOne(filter = "_id" $eq userID.$oid, update = "netWorth" $inc amount)
    }

    @inline
    def findOneByID(userID: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneFuture[ProfileData]("_id" $eq userID.$oid)
    }

    @inline
    def findOneByFacebookID(fbId: String)(implicit ec: ExecutionContext) = {
      dao.findOneFuture[ProfileData]("facebookID" $eq fbId)
    }

    @inline
    def findOneOrCreateByFacebook(fbProfile: ProfileData, fbId: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      fbProfile._id = mongo.ObjectID()
      dao.findOneAndUpdate(filter = "facebookID" $eq fbId, update = doc(
        "$setOnInsert" -> fbProfile// ,
        //"lastLoginTime" $set new js.Date()
      ), new FindAndUpdateOptions(upsert = true, returnOriginal = false))
    }

    @inline
    def addFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneAndUpdate(filter = "_id" $eq userID.$oid, update = "favoriteSymbols" $addToSet symbol)
    }

    @inline
    def addRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneAndUpdate(filter = "_id" $eq userID.$oid, update = "recentSymbols" $addToSet symbol)
    }

    @inline
    def removeFavoriteSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneAndUpdate(filter = "_id" $eq userID.$oid, update = "favoriteSymbols" $pull symbol)
    }

    @inline
    def removeRecentSymbol(userID: String, symbol: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneAndUpdate(filter = "_id" $eq userID.$oid, update = "recentSymbols" $pull symbol)
    }

  }

  /**
    * Profile DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class ProfileDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getProfileDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Players").mapTo[ProfileDAO]
    }
  }

}
