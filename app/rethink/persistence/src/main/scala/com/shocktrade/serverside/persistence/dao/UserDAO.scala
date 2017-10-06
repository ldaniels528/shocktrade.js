package com.shocktrade.serverside.persistence.dao

import com.shocktrade.serverside.persistence.dao.mysql.UserDAOMySQL

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * User DAO
  * @author lawrence.daniels@gmail.com
  */
trait UserDAO {

  def findByName(name: String): Future[js.Array[UserData]]

}

/**
  * User DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object UserDAO {

  def apply()(implicit ec: ExecutionContext): UserDAO = new UserDAOMySQL()

}