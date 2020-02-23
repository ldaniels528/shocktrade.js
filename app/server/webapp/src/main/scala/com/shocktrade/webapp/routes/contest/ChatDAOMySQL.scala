package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.models.contest.ChatMessage
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Chat DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ChatDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with ChatDAO {

  override def addChatMessage(contestID: String, playerID: String, message: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    conn.executeFuture(
      """|INSERT INTO contest_chats (messageID, contestID, playerID, message)
         |VALUES (now(), ?, ?, ?)
         |""".stripMargin,
      js.Array(contestID, playerID, message)) map (_.affectedRows > 0)
  }

  override def findChatMessages(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[ChatMessage]] = {
    conn.queryFuture[ChatMessage]("SELECT * FROM contest_chats WHERE contestID = ?", js.Array(contestID)) map { case (rows, _) => rows }
  }

}
