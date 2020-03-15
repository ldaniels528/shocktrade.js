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

  override def addChatMessage(contestID: String, userID: String, message: String)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture(
      """|INSERT INTO contest_chats (messageID, contestID, userID, message)
         |VALUES (now(), ?, ?, ?)
         |""".stripMargin,
      js.Array(contestID, userID, message)) map (_.affectedRows)
  }

  override def findChatMessages(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[ChatMessage]] = {
    conn.queryFuture[ChatMessage](
      """|SELECT CC.*, U.username
         |FROM contest_chats CC
         |INNER JOIN users U ON U.userID = CC.userID
         |WHERE CC.contestID = ?
         |""".stripMargin,
      js.Array(contestID)) map { case (rows, _) => rows }
  }

}
