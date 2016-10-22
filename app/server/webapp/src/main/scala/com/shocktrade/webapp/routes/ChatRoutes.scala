package com.shocktrade.webapp.routes

import java.util.UUID

import com.shocktrade.server.dao.contest.ContestDAO._
import com.shocktrade.server.dao.contest.ContestData
import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.common.models.contest.ChatMessage
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}
import org.scalajs.nodejs.{NodeRequire, console}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Chat Message Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ChatRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) = {
    implicit val contestDAO = dbFuture.flatMap(_.getContestDAO)

    app.get("/api/contest/:id/chat", (request: Request, response: Response, next: NextFunction) => chatMessages(request, response, next))
    app.post("/api/contest/:id/chat", (request: Request, response: Response, next: NextFunction) => addChatMessage(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def addChatMessage(request: Request, response: Response, next: NextFunction) = {
      val contestID = request.params("id")
      val rawMessage = request.bodyAs[ChatMessage]
      val message = rawMessage.copy(_id = UUID.randomUUID().toString, sentTime = new js.Date())
      contestDAO.flatMap(_.addChatMessage(contestID, message).toFuture) onComplete {
        case Success(result) if result.isOk =>
          result.valueAs[ContestData] match {
            case Some(contest) =>
              response.send(contest.messages)
              next()
              WebSocketHandler.emit(RemoteEvent.ChatMessagesUpdated, contestID)
            case None => response.notFound("Unexpected error"); next()
          }
        case Success(result) =>
          console.log("failed result = %j", result)
          response.badRequest("Message could not be created")
          next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def chatMessages(request: Request, response: Response, next: NextFunction) = {
      val contestID = request.params("id")
      contestDAO.flatMap(_.findChatMessages(contestID)) onComplete {
        case Success(Some(messages)) => response.send(messages); next()
        case Success(None) => response.notFound("Unexpected error"); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

  }

}
