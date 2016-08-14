package com.shocktrade.javascript.data

import com.shocktrade.javascript.models.Profile
import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Profile DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ProfileDAO extends Collection

/**
  * Profile DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object ProfileDAO {

  /**
    * Profile DAO Extensions
    * @param profileDAO the given [[ProfileDAO profile DAO]]
    */
  implicit class ProfileDAOExtensions(val profileDAO: ProfileDAO) extends AnyVal {

    @inline
    def findByFacebookID(fbId: String)(implicit ec: ExecutionContext) = {
      profileDAO.findOneFuture[Profile]("facebookID" $eq fbId)
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
