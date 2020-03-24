package com.shocktrade.webapp.routes
package contest

import com.shocktrade.common.Ok
import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.common.forms.{ContestCreationForm, ContestSearchForm, ValidationErrors}
import com.shocktrade.common.models.contest.ChatMessage
import com.shocktrade.common.util.StringHelper._
import com.shocktrade.webapp.routes.contest.dao._
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Contest Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val contestDAO = ContestDAO()
  private val perksDAO = PerksDAO()
  private val positionDAO = PositionDAO()

  // individual contests
  app.get("/api/contest/:id", (request: Request, response: Response, next: NextFunction) => findContestByID(request, response, next))
  app.post("/api/contest", (request: Request, response: Response, next: NextFunction) => createContest(request, response, next))

  // charts
  app.get("/api/contest/:id/user/:userID/chart/:chart", (request: Request, response: Response, next: NextFunction) => findChart(request, response, next))

  // chat messages
  app.get("/api/contest/:id/chat", (request: Request, response: Response, next: NextFunction) => listChatMessages(request, response, next))
  app.post("/api/contest/:id/chat", (request: Request, response: Response, next: NextFunction) => addChatMessage(request, response, next))

  // contest participation
  app.delete("/api/contest/:id/user/:userID", (request: Request, response: Response, next: NextFunction) => quitContest(request, response, next))
  app.put("/api/contest/:id/user/:userID", (request: Request, response: Response, next: NextFunction) => joinContest(request, response, next))

  // collections of contests
  app.get("/api/contest/:id/rankings", (request: Request, response: Response, next: NextFunction) => listRankings(request, response, next))
  app.get("/api/contests/perks", (request: Request, response: Response, next: NextFunction) => listPerks(request, response, next))
  app.get("/api/contests/user/:userID", (request: Request, response: Response, next: NextFunction) => findMyContests(request, response, next))
  app.post("/api/contests/search", (request: Request, response: Response, next: NextFunction) => search(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def addChatMessage(request: Request, response: Response, next: NextFunction): Unit = {
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
        contestDAO.addChatMessage(contestID, userID, message) onComplete {
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

  def listChatMessages(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("id")
    contestDAO.findChatMessages(contestID) onComplete {
      case Success(messages) => response.send(messages); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves available perks
   */
  def listPerks(request: Request, response: Response, next: NextFunction): Unit = {
    perksDAO.findAvailablePerks onComplete {
      case Success(perks) => response.send(perks); next()
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
   * Creates a new contest
   */
  def createContest(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.bodyAs[ContestCreationForm]
    form.validate match {
      case messages if messages.nonEmpty =>
        response.badRequest(new ValidationErrors(messages)); next()
      case _ =>
        contestDAO.create(form) onComplete {
          case Success(Some(result)) => response.send(result); next()
          case Success(None) => response.badRequest(form); next()
          case Failure(e) => response.internalServerError(e); next()
        }
    }
  }

  def findChart(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID, chart) = (request.params("id"), request.params("userID"), request.params("chart"))
    positionDAO.findChart(contestID, userID, chart) onComplete {
      case Success(data) => response.send(data); next()
      case Failure(e) => e.printStackTrace(); response.internalServerError(e); next()
    }
  }

  def joinContest(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("id"), request.params("userID"))
    contestDAO.join(contestID, userID) onComplete {
      case Success(data) => response.send(data); next()
      case Failure(e) => e.printStackTrace(); response.internalServerError(e); next()
    }
  }

  def quitContest(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("id"), request.params("userID"))
    contestDAO.quit(contestID, userID) onComplete {
      case Success(data) => response.send(data); next()
      case Failure(e) => e.printStackTrace(); response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves contests by userID
   */
  def findMyContests(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val outcome = for {
      myContests <- contestDAO.findMyContests(userID)
      sortedContests = myContests.sortBy(-_.playerGainLoss.getOrElse(-Double.MaxValue))
    } yield sortedContests.zipWithIndex map { case (ranking, rank) => ranking.copy(playerRank = rank + 1, leaderRank = 1) }

    outcome onComplete {
      case Success(contests) => response.send(contests); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves a collection of rankings by contest
   */
  def listRankings(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("id")
    val outcome = for {
      rankings <- contestDAO.findRankings(contestID)

      // sort the rankings and add the position (e.g. "1st")
      sortedRankings = {
        val myRankings = rankings.sortBy(-_.gainLoss.getOrElse(0.0))
        myRankings.zipWithIndex foreach { case (ranking, index) =>
          ranking.rank = (index + 1).nth
        }
        js.Array(myRankings: _*)
      }

    } yield sortedRankings

    outcome onComplete {
      case Success(rankings) => response.send(rankings); next()
      case Failure(e) =>
        response.internalServerError(e); next()
    }
  }

  /**
   * Searches for contest via a [[ContestSearchForm contest search form]]
   */
  def search(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.bodyAs[ContestSearchForm]
    form.validate match {
      case messages if messages.isEmpty =>
        contestDAO.search(form) onComplete {
          case Success(contests) => response.send(contests); next()
          case Failure(e) => response.showException(e).internalServerError(e); next()
        }
      case messages =>
        response.badRequest(new ValidationErrors(messages)); next()
    }
  }

}
