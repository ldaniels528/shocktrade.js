package com.shocktrade.server.dao.contest

import com.shocktrade.common.forms.ContestSearchForm
import com.shocktrade.common.models.contest.{ChatMessage, Participant}
import com.shocktrade.server.dao.DataAccessObjectHelper
import com.shocktrade.server.dao.contest.mysql.ContestDAOMySQL
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Contest DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait ContestDAO {

  /**
    *
    * @param contestID
    * @param message
    * @param ec
    * @return
    */
  def addChatMessage(contestID: String, playerID: String, message: String)(implicit ec: ExecutionContext): Future[Boolean]

  /**
    *
    * @param contest
    * @return
    */
  def create(contest: ContestData)(implicit ec: ExecutionContext): Future[Boolean]

  /**
    *
    * @param ec
    * @return
    */
  def findActiveContests()(implicit ec: ExecutionContext): Future[js.Array[ContestData]]

  /**
    *
    * @param contestID
    * @param ec
    * @return
    */
  def findChatMessages(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[ChatMessage]]

  /**
    *
    * @param contestID
    * @param ec
    * @return
    */
  def findOneByID(contestID: String)(implicit ec: ExecutionContext): Future[Option[ContestData]]

  /**
    *
    * @param playerID
    * @param ec
    * @return
    */
  def findByPlayer(playerID: String)(implicit ec: ExecutionContext): Future[js.Array[ContestData]]

  /**
    *
    * @param playerID
    * @param ec
    * @return
    */
  def findUnoccupied(playerID: String)(implicit ec: ExecutionContext): Future[js.Array[ContestData]] = ???

  /**
    *
    * @param contestID
    * @param userID
    * @param ec
    * @return
    */
  def join(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Boolean]

  /**
    *
    * @param form
    * @return
    */
  def search(form: ContestSearchForm)(implicit ec: ExecutionContext): Future[js.Array[ContestData]]

  /**
    *
    * @param contest
    * @return
    */
  def updateContest(contest: ContestData)(implicit ec: ExecutionContext): Future[Int]

  /**
    *
    * @param contests
    * @return
    */
  def updateContests(contests: Seq[ContestData])(implicit ec: ExecutionContext): Future[Int]

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