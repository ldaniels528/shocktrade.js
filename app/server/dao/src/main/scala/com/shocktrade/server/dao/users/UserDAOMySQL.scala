package com.shocktrade.server.dao.users

import io.scalajs.npm.mysql.{ConnectionOptions, MySQL}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * MySQL implementation of the User DAO
  * @author lawrence.daniels@gmail.com
  */
class UserDAOMySQL(options: ConnectionOptions) extends UserDAO {
  private val datePattern = "YYYY-MM-DD HH:mm:ss"
  private val conn = MySQL.createConnection(options)

  /**
    * Attempts to retrieve a user by ID
    * @param id the given user ID
    * @return a promise of an option of a user
    */
  override def findByID(id: String)(implicit ec: ExecutionContext): Future[Option[UserData]] = {
    conn.queryFuture[UserData](s"SELECT * FROM users WHERE name = '$id'") map { case (rows, _) =>
      rows.headOption
    }
  }

  /**
    * Attempts to retrieve a user by ID
    * @param ids the given collection of user IDs
    * @return a promise of an option of a user
    */
  override def findByIDs(ids: Seq[String])(implicit ec: ExecutionContext): Future[js.Array[UserData]] = {
    conn.queryFuture[UserData](s"SELECT * FROM users WHERE id IN (${ids.map(id => s"'$id'").mkString(",")})") map { case (rows, _) => rows }
  }

  /**
    * Attempts to retrieve a user by username
    * @param name the given username
    * @return a promise a collection of users
    */
  override def findByUsername(name: String)(implicit ec: ExecutionContext): Future[js.Array[UserData]] = {
    conn.queryFuture[UserData](s"SELECT * FROM users WHERE name like '$name%'") map { case (rows, _) =>
      rows
    }
  }

}

