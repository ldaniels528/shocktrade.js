package com.shocktrade.webapp.routes
package contest

import com.shocktrade.common.Ok
import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.common.forms.{ContestCreationForm, ContestSearchForm, ValidationErrors}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking}
import com.shocktrade.common.util.StringHelper._
import com.shocktrade.webapp.routes.contest.dao._
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Contest Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestRoutes(app: Application)(implicit ec: ExecutionContext, contestDAO: ContestDAO, perksDAO: PerksDAO, positionDAO: PositionDAO) {
  // individual contests
  app.get("/api/contest/:id", (request: Request, response: Response, next: NextFunction) => findContestByID(request, response, next))
  app.post("/api/contest", (request: Request, response: Response, next: NextFunction) => createContest(request, response, next))

  // charts
  app.get("/api/contest/:id/user/:userID/chart/:chart", (request: Request, response: Response, next: NextFunction) => findChart(request, response, next))

  // chat messages
  app.get("/api/contest/:id/chat", (request: Request, response: Response, next: NextFunction) => findChatMessages(request, response, next))
  app.post("/api/contest/:id/chat", (request: Request, response: Response, next: NextFunction) => addChatMessage(request, response, next))

  // contest participation
  app.delete("/api/contest/:id/user/:userID", (request: Request, response: Response, next: NextFunction) => quitContest(request, response, next))
  app.put("/api/contest/:id/user/:userID", (request: Request, response: Response, next: NextFunction) => joinContest(request, response, next))

  // collections of contests
  app.get("/api/contest/:id/rankings", (request: Request, response: Response, next: NextFunction) => findRankings(request, response, next))
  app.get("/api/contests/perks", (request: Request, response: Response, next: NextFunction) => findPerks(request, response, next))
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

  def findChatMessages(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("id")
    contestDAO.findChatMessages(contestID) onComplete {
      case Success(messages) => response.send(messages); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves available perks
   */
  def findPerks(request: Request, response: Response, next: NextFunction): Unit = {
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
          case Failure(e) => e.printStackTrace(); response.internalServerError(e); next()
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
      case Success(data) => response.send(Ok(data)); next()
      case Failure(e) => e.printStackTrace(); response.internalServerError(e); next()
    }
  }

  def quitContest(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("id"), request.params("userID"))
    contestDAO.quit(contestID, userID) onComplete {
      case Success(data) => response.send(Ok(data)); next()
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
      case Failure(e) => e.printStackTrace(); response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves a collection of rankings by contest
   */
  def findRankings(request: Request, response: Response, next: NextFunction): Unit = {
    // define an accumulator for determining the rankings
    case class Accumulator(rankings: List[ContestRanking] = Nil, lastRanking: Option[ContestRanking] = None, index: Int = 1)

    // retrieve the rankings
    val contestID = request.params("id")
    val outcome = contestDAO.findRankings(contestID) map { rankings =>
      // sort the rankings and add the position (e.g. "1st")
      val results = rankings.sortBy(-_.gainLoss.orZero).foldLeft[Accumulator](Accumulator()) {
        case (acc@Accumulator(rankings, lastRanking, index), ranking) =>
          val newIndex = if (lastRanking.exists(_.totalEquity.exists(_ > ranking.totalEquity.orZero))) index + 1 else index
          val newRanking = ranking.copy(rank = newIndex.nth, rankNum = newIndex)
          acc.copy(rankings = newRanking :: rankings, lastRanking = Some(ranking), index = newIndex)
      }
      js.Array(results.rankings: _*)
    }

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
