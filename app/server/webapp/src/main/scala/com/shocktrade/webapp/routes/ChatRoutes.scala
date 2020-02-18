package com.shocktrade.webapp.routes

import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.server.dao.contest.ContestDAO
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.Db

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Chat Message Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ChatRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext): Unit = {
    implicit val contestDAO: ContestDAO = ContestDAO()

    app.get("/api/contest/:id/chat", (request: Request, response: Response, next: NextFunction) => listChatMessages(request, response, next))
    app.post("/api/contest/:id/player/:playerId/chat", (request: Request, response: Response, next: NextFunction) => addChatMessage(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def addChatMessage(request: Request, response: Response, next: NextFunction): Unit = {
      // get the arguments
      val form = for {
        contestID <- request.params.get("id")
        playerID <- request.params.get("playerId")
        newMessage <- request.body.asInstanceOf[js.UndefOr[String]].toOption
      } yield (contestID, playerID, newMessage)

      // handle the request
      form match {
        case Some((contestID, playerID, newMessage)) =>
          // asynchronously create the message
          val outcome = for {
            isCreated <- contestDAO.addChatMessage(contestID, playerID, newMessage) if isCreated
            messages <- contestDAO.findChatMessages(contestID)
          } yield messages

          outcome onComplete {
            // HTTP/200 OK
            case Success(messages) =>
              response.send(messages)
              WebSocketHandler.emit(RemoteEvent.ChatMessagesUpdated, contestID)
              next()
            // HTTP/500 ERROR
            case Failure(e) =>
              response.internalServerError(e); next()
          }
        // HTTP/404 NOT FOUND
        case None =>
          response.notFound("User not found"); next()
      }
    }

    def listChatMessages(request: Request, response: Response, next: NextFunction): Unit = {
      val contestID = request.params("id")
      contestDAO.findChatMessages(contestID) onComplete {
        case Success(messages) => response.send(messages); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

  }

}
