package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.forms.ContestSearchRequest
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking, ContestSearchResult}
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Contest DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ContestDAO {

  def findOneByID(contestID: String): Future[Option[ContestData]]

  def findRankings(contestID: String): Future[js.Array[ContestRanking]]

  def contestSearch(form: ContestSearchRequest): Future[js.Array[ContestSearchResult]]

  def findChatMessages(contestID: String): Future[js.Array[ChatMessage]]

}

/**
 * Contest DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestDAO {

  /**
   * Creates a new Contest DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[ContestDAO Contest DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions)
           (implicit ec: ExecutionContext): ContestDAO = new ContestDAOMySQL(options)

}