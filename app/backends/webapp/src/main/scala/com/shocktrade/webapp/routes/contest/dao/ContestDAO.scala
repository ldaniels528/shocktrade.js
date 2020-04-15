package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.forms.{ContestCreationRequest, ContestCreationResponse, ContestSearchForm}
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

  def create(request: ContestCreationRequest): Future[ContestCreationResponse]

  def create(portfolio: PortfolioData): Future[Int]

  def findOneByID(contestID: String): Future[Option[ContestData]]

  def findRankings(contestID: String): Future[js.Array[ContestRanking]]

  def contestSearch(form: ContestSearchForm): Future[js.Array[ContestSearchResult]]

  def start(contestID: String, userID: String): Future[Boolean]

  def updateContest(contest: ContestData): Future[Int]

  def updateContests(contests: Seq[ContestData]): Future[Int]

  def putChatMessage(contestID: String, userID: String, message: String): Future[Int]

  def findChatMessages(contestID: String): Future[js.Array[ChatMessage]]

  def join(contestID: String, userID: String): Future[Int]

  def quit(contestID: String, userID: String): Future[Int]

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