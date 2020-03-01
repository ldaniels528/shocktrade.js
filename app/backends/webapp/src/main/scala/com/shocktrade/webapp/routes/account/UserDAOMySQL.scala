package com.shocktrade.webapp.routes.account

import com.shocktrade.webapp.routes.social.IconData
import io.scalajs.npm.mysql.{MySQL, MySQLConnectionOptions}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * MySQL implementation of the User DAO
  * @author lawrence.daniels@gmail.com
  */
class UserDAOMySQL(options: MySQLConnectionOptions) extends UserDAO {
  private val conn = MySQL.createConnection(options)

  override def findByID(id: String)(implicit ec: ExecutionContext): Future[Option[UserData]] = {
    conn.queryFuture[UserData](s"SELECT * FROM users WHERE name = '$id'") map { case (rows, _) =>
      rows.headOption
    }
  }

  override def findByIDs(ids: Seq[String])(implicit ec: ExecutionContext): Future[js.Array[UserData]] = {
    conn.queryFuture[UserData](s"SELECT * FROM users WHERE id IN (${ids.map(id => s"'$id'").mkString(",")})") map { case (rows, _) => rows }
  }

  override def findByUsername(name: String)(implicit ec: ExecutionContext): Future[js.Array[UserData]] = {
    conn.queryFuture[UserData](s"SELECT * FROM users WHERE name like '$name%'") map { case (rows, _) =>
      rows
    }
  }

  override def createIcon(userID: String, name: String, mime: String, blob: js.Any)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture("INSERT INTO user_icons (iconID, name, mime, image) VALUES (?, ?, ?, ?)", js.Array(userID, name, mime, blob))
      .map(_.affectedRows)
  }

  override def findIcon(userID: String)(implicit ec: ExecutionContext): Future[Option[IconData]] = {
    conn.queryFuture[IconData]("SELECT * FROM user_icons WHERE iconID = ?", js.Array(userID))
      .map { case (rows, _) => rows.headOption }
  }

}

