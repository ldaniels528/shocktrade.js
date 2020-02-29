package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.models.contest.ChatMessage
import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Chat DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ChatDAO {

  /**
   *
   * @param contestID
   * @param message
   * @param ec
   * @return
   */
  def addChatMessage(contestID: String, portfolioID: String, message: String)(implicit ec: ExecutionContext): Future[Int]

  /**
   *
   * @param contestID
   * @param ec
   * @return
   */
  def findChatMessages(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[ChatMessage]]

}

/**
 * Chat DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ChatDAO {

  /**
   * Creates a new Chat DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[ChatDAO Chat DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): ChatDAO = new ChatDAOMySQL(options)

}
