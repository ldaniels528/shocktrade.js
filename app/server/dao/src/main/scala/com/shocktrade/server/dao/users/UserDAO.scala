package com.shocktrade.server.dao
package users

import io.scalajs.npm.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * User DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait UserDAO extends Collection

/**
  * User DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object UserDAO {

  /**
    * User DAO Enrichment
    * @param dao the given [[UserDAO user DAO]]
    */
  implicit class UserDAOEnrichment(val dao: UserDAO) extends AnyVal {

    @inline
    def findUserWithFields[T <: js.Any](id: String, fields: js.Array[String])(implicit ec: ExecutionContext) = {
      dao.findOneFuture[T](selector = "_id" $eq id.$oid, fields = fields)
    }

    @inline
    def findUserByID(id: String)(implicit ec: ExecutionContext) = {
      dao.findById[UserData](id, fields = js.Array("facebookID", "name"))
    }

    @inline
    def findUsersByID(ids: js.Array[String])(implicit ec: ExecutionContext) = {
      dao.find("_id" $in ids.map(_.$oid), projection = js.Dictionary("facebookID" -> 1, "name" -> 1)).toArrayFuture[UserData]
    }

    @inline
    def findFriendByFacebookID(id: String)(implicit ec: ExecutionContext) = {
      dao.findOneFuture[FriendStatusData]("facebookID" $eq id, fields = FriendStatusData.Fields)
    }

  }

  /**
    * User DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class UserDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getUserDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Players").mapTo[UserDAO]
    }
  }

}