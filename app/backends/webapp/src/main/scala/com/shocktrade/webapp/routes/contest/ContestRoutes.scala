package com.shocktrade.webapp.routes
package contest

import com.shocktrade.common.Ok
import com.shocktrade.common.api.ContestAPI
import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.common.forms.{ContestCreationRequest, ContestSearchForm, ValidationErrors}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking}
import com.shocktrade.webapp.routes.contest.dao._
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Contest Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestRoutes(app: Application)(implicit ec: ExecutionContext, contestDAO: ContestDAO) extends ContestAPI {
  // individual contests
  app.post(createNewGameURL, (request: Request, response: Response, next: NextFunction) => createContest(request, response, next))
  app.get(findContestByIDURL(":id"), (request: Request, response: Response, next: NextFunction) => findContestByID(request, response, next))

  // chat messages
  app.get(findChatMessagesURL(":id"), (request: Request, response: Response, next: NextFunction) => findChatMessages(request, response, next))
  app.post(putChatMessageURL(":id"), (request: Request, response: Response, next: NextFunction) => putChatMessage(request, response, next))

  // contest participation
  app.delete(quitContestURL(":id", ":userID"), (request: Request, response: Response, next: NextFunction) => quitContest(request, response, next))
  app.put(joinContestURL(":id", ":userID"), (request: Request, response: Response, next: NextFunction) => joinContest(request, response, next))

  // collections of contests
  app.put(contestSearchURL, (request: Request, response: Response, next: NextFunction) => contestSearch(request, response, next))
  app.get(findContestRankingsURL(":id"), (request: Request, response: Response, next: NextFunction) => findContestRankings(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Searches for contest via a [[ContestSearchForm contest search form]]
   */
  def contestSearch(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.bodyAs[ContestSearchForm]
    form.validate match {
      case messages if messages.isEmpty =>
        contestDAO.contestSearch(form) onComplete {
          case Success(contests) =>
            response.setContentType("application/json")
            response.send(contests)
            next()
          case Failure(e) => response.showException(e).internalServerError(e); next()
        }
      case messages =>
        response.badRequest(new ValidationErrors(messages)); next()
    }
  }

  /**
   * Creates a new contest
   */
  def createContest(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.bodyAs[ContestCreationRequest]
    form.validate match {
      case messages if messages.nonEmpty =>
        response.badRequest(new ValidationErrors(messages)); next()
      case _ =>
        contestDAO.create(form) onComplete {
          case Success(result) => response.send(result); next()
          case Failure(e) => response.showException(e).internalServerError(e); next()
        }
    }
  }

  def findChatMessages(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("id")
    contestDAO.findChatMessages(contestID) onComplete {
      case Success(messages) => response.setContentType("application/json"); response.send(messages); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves contests by portfolio
   */
  def findContestByID(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("id")
    contestDAO.findOneByID(contestID) onComplete {
      case Success(Some(contest)) => response.send(contest); next()
      case Success(None) => response.notFound(contestID); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves a collection of rankings by contest
   */
  def findContestRankings(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("id")
    contestDAO.findRankings(contestID).map(rankings => ContestRanking.computeRankings(rankings.toSeq)) onComplete {
      case Success(rankings) => response.setContentType("application/json"); response.send(rankings.toJSArray); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def joinContest(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("id"), request.params("userID"))
    contestDAO.join(contestID, userID) onComplete {
      case Success(data) => response.send(Ok(data)); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def putChatMessage(request: Request, response: Response, next: NextFunction): Unit = {
    // get the arguments
    val form = for {
      contestID <- request.params.get("id")
      chatMessage = request.bodyAs[ChatMessage]
      userID <- chatMessage.userID.toOption
      message <- chatMessage.message.toOption
    } yield (contestID, userID, message)

    // handle the request
    form match {
      case Some((contestID, userID, message)) =>
        // asynchronously create the message
        contestDAO.putChatMessage(contestID, userID, message) onComplete {
          // HTTP/200 OK
          case Success(count) =>
            response.send(Ok(count))
            WebSocketHandler.emit(RemoteEvent.ChatMessagesUpdated, contestID)
            next()
          // HTTP/500 ERROR
          case Failure(e) =>
            response.internalServerError(e); next()
        }
      // HTTP/404 NOT FOUND
      case None =>
        response.notFound(request.params); next()
    }
  }

  def quitContest(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("id"), request.params("userID"))
    contestDAO.quit(contestID, userID) onComplete {
      case Success(data) => response.send(Ok(data)); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

}
