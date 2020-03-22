package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.forms.{ContestCreationForm, ContestCreationResponse, ContestSearchForm}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking, ContestSearchResult, MyContest}
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Contest DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ContestDAO {

  def create(form: ContestCreationForm)(implicit ec: ExecutionContext): Future[Option[ContestCreationResponse]]

  def findOneByID(contestID: String)(implicit ec: ExecutionContext): Future[Option[ContestData]]

  def findMyContests(userID: String)(implicit ec: ExecutionContext): Future[js.Array[MyContest]]

  def findRankings(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[ContestRanking]]

  def join(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Boolean]

  def quit(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Boolean]

  def search(form: ContestSearchForm)(implicit ec: ExecutionContext): Future[js.Array[ContestSearchResult]]

  def start(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Boolean]

  def updateContest(contest: ContestData)(implicit ec: ExecutionContext): Future[Int]

  def updateContests(contests: Seq[ContestData])(implicit ec: ExecutionContext): Future[Int]

  def addChatMessage(contestID: String, userID: String, message: String)(implicit ec: ExecutionContext): Future[Int]

  def findChatMessages(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[ChatMessage]]

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
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): ContestDAO = new ContestDAOMySQL(options)

}