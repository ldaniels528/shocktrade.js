package com.shocktrade.common.dao.contest

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
    * Profile DAO Extensions
    * @param dao the given [[ProfileDAO profile DAO]]
    */
  implicit class ProfileDAOExtensions(val dao: ProfileDAO) extends AnyVal {

    @inline
    def findOneByID(userID: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneFuture[ProfileData]("_id" $eq userID.$oid)
    }

    @inline
    def findOneByFacebookID(fbId: String)(implicit ec: ExecutionContext) = {
      dao.findOneFuture[ProfileData]("facebookID" $eq fbId)
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
