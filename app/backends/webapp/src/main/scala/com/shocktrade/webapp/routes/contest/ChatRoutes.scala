package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.webapp.routes.{NextFunction, WebSocketHandler}
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Chat Message Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ChatRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val chatDAO = ChatDAO()

  app.get("/api/contest/:id/chat", (request: Request, response: Response, next: NextFunction) => listChatMessages(request, response, next))
  app.post("/api/contest/:id/player/:portfolioID/chat", (request: Request, response: Response, next: NextFunction) => addChatMessage(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def addChatMessage(request: Request, response: Response, next: NextFunction): Unit = {
    // get the arguments
    val form = for {
      contestID <- request.params.get("id")
      portfolioID <- request.params.get("portfolioID")
      newMessage <- request.body.asInstanceOf[js.UndefOr[String]].toOption
    } yield (contestID, portfolioID, newMessage)

    // handle the request
    form match {
      case Some((contestID, portfolioID, newMessage)) =>
        // asynchronously create the message
        val outcome = for {
          count <- chatDAO.addChatMessage(contestID, portfolioID, newMessage) if count == 1
          messages <- chatDAO.findChatMessages(contestID)
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
    chatDAO.findChatMessages(contestID) onComplete {
      case Success(messages) => response.send(messages); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

}
