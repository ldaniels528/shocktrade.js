package com.shocktrade.serverside.persistence.dao.mysql

import com.shocktrade.serverside.persistence.dao.{UserDAO, UserData}
import io.scalajs.npm.mysql.{MySQL, MySQLConnectionOptions}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * MySQL implementation of the User DAO
  * @author lawrence.daniels@gmail.com
  */
class UserDAOMySQL()(implicit ec: ExecutionContext) extends UserDAO {
  private val datePattern = "YYYY-MM-DD HH:mm:ss"
  private val conn = MySQL.createConnection(new MySQLConnectionOptions(
    host = "dev001",
    database = "shocktrade",
    user = "webapp",
    password = "shock1"
  ))

  override def findByName(name: String): Future[js.Array[UserData]] = {
    conn.queryFuture[UserData](s"SELECT * FROM users WHERE name = '$name'") map { case (rows, _) =>
      rows
    }
  }

}
