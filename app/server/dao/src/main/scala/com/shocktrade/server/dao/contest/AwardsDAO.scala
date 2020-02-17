package com.shocktrade.server.dao.contest

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.ConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Awards DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait AwardsDAO {

  /**
    * Retrieves the collection of available awards
    * @param ec  the implicit [[ExecutionContext]]
    * @return the collection of available [[AwardData]]
    */
  def findAvailableAwards(implicit ec: ExecutionContext): Future[js.Array[AwardData]]

}

/**
  * Awards DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object AwardsDAO {

  /**
    * Creates a new User DAO instance
    * @param options the given [[ConnectionOptions]]
    * @return a new [[AwardsDAO User DAO]]
    */
  def apply(options: ConnectionOptions = DataAccessObjectHelper.getConnectionOptions): AwardsDAO = new AwardsDAOMySQL(options)

}