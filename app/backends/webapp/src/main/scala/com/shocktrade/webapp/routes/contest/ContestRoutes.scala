package com.shocktrade.webapp.routes
package contest

import java.util.UUID
import scala.scalajs.js.JSConverters._
import com.shocktrade.common.api.ContestAPI
import com.shocktrade.common.forms.{ContestCreationRequest, ContestSearchRequest, ValidationErrors}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking}
import com.shocktrade.webapp.routes.contest.dao._
import com.shocktrade.webapp.vm.VirtualMachine
import com.shocktrade.webapp.vm.dao.VirtualMachineDAO
import com.shocktrade.webapp.vm.opcodes.{CreateContest, JoinContest, QuitContest, SendChatMessage}
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Contest Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestRoutes(app: Application)(implicit ec: ExecutionContext, contestDAO: ContestDAO, vmDAO: VirtualMachineDAO, vm: VirtualMachine)
  extends ContestAPI {

  // individual contests
  app.post(createContestURL, (request: Request, response: Response, next: NextFunction) => createContest(request, response, next))
  app.get(findContestByIDURL(":id"), (request: Request, response: Response, next: NextFunction) => findContestByID(request, response, next))

  // chat messages
  app.get(findChatMessagesURL(":id"), (request: Request, response: Response, next: NextFunction) => findChatMessages(request, response, next))
  app.post(sendChatMessageURL(":id"), (request: Request, response: Response, next: NextFunction) => sendChatMessage(request, response, next))

  // contest participation
  app.delete(quitContestURL(":id", ":userID"), (request: Request, response: Response, next: NextFunction) => quitContest(request, response, next))
  app.put(joinContestURL(":id", ":userID"), (request: Request, response: Response, next: NextFunction) => joinContest(request, response, next))

  // collections of contests
  app.get(contestSearchURL(), (request: Request, response: Response, next: NextFunction) => contestSearch(request, response, next))
  app.get(findContestRankingsURL(":id"), (request: Request, response: Response, next: NextFunction) => findContestRankings(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Searches for contest via a [[ContestSearchRequest contest search form]]
   */
  def contestSearch(request: Request, response: Response, next: NextFunction): Unit = {
    val form = new ContestSearchRequest(
      userID =request.query.get("userID").orUndefined,
      buyIn = request.query.get("buyIn").orUndefined.map(_.toDouble),
      continuousTrading = request.query.get("continuousTrading").orUndefined.map(_.toBoolean),
      duration = request.query.get("duration").orUndefined.map(_.toInt),
      friendsOnly = request.query.get("friendsOnly").orUndefined.map(_.toBoolean),
      invitationOnly = request.query.get("invitationOnly").orUndefined.map(_.toBoolean),
      levelCap = request.query.get("levelCap").orUndefined.map(_.toInt),
      levelCapAllowed = request.query.get("levelCapAllowed").orUndefined.map(_.toBoolean),
      myGamesOnly = request.query.get("myGamesOnly").orUndefined.map(_.toBoolean),
      nameLike = request.query.get("nameLike").orUndefined,
      perksAllowed = request.query.get("perksAllowed").orUndefined.map(_.toBoolean),
      robotsAllowed = request.query.get("robotsAllowed").orUndefined.map(_.toBoolean),
      statusID = request.query.get("statusID").orUndefined.map(_.toInt)
    )

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
    val form = request.bodyAs[ContestCreationRequest].copy(contestID = UUID.randomUUID().toString)
    form.validate match {
      case messages if messages.nonEmpty =>
        response.badRequest(new ValidationErrors(messages)); next()
      case _ =>
        vm.invoke(CreateContest(form)) onComplete {
          case Success(result) => response.send(result); next()
          case Failure(e) => response.showException(e).internalServerError(e); next()
        }
    }
  }

  def findChatMessages(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("id")
    contestDAO.findChatMessages(contestID) onComplete {
      case Success(messages) => response.setContentType("application/json"); response.send(messages); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
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
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Retrieves a collection of rankings by contest
   */
  def findContestRankings(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("id")
    contestDAO.findRankings(contestID).map(ContestRanking.computeRankings) onComplete {
      case Success(rankings) => response.setContentType("application/json"); response.send(rankings); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def joinContest(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("id"), request.params("userID"))
    vm.invoke(JoinContest(contestID, userID)) onComplete {
      case Success(result) => response.send(result); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def sendChatMessage(request: Request, response: Response, next: NextFunction): Unit = {
    // get the arguments
    val form = for {
      contestID <- request.params.get("id")
      chatMessage = request.bodyAs[ChatMessage]
      userID <- chatMessage.userID.toOption
      message <- chatMessage.message.toOption
    } yield (contestID, userID, message, chatMessage)

    // handle the request
    form match {
      case Some((contestID, userID, message, chatMessage)) =>
        // asynchronously create the message
        vm.invoke(SendChatMessage(contestID, userID, message)) onComplete {
          // HTTP/200 OK
          case Success(result) =>
            response.send(result)
            //WebSocketHandler.emit(RemoteEvent.createMessageEvent(chatMessage))
            next()
          // HTTP/500 ERROR
          case Failure(e) =>
            response.showException(e).internalServerError(e); next()
        }
      // HTTP/404 NOT FOUND
      case None =>
        response.notFound(request.params); next()
    }
  }

  def quitContest(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("id"), request.params("userID"))
    vm.invoke(QuitContest(contestID, userID)) onComplete {
      case Success(result) => response.send(result); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

}
