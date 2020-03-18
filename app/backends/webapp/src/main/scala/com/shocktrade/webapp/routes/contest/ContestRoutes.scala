package com.shocktrade.webapp.routes
package contest

import com.shocktrade.common.forms.{ContestCreationForm, ContestSearchForm, ValidationErrors}
import com.shocktrade.common.util.StringHelper._
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
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
  app.get("/api/charts/exposure/:chart/:id/:userID", (request: Request, response: Response, next: NextFunction) => exposureChart(request, response, next))
  app.get("/api/contest/:id", (request: Request, response: Response, next: NextFunction) => contestByID(request, response, next))
  app.post("/api/contest", (request: Request, response: Response, next: NextFunction) => createContest(request, response, next))

  // collections of contests
  app.get("/api/contest/:contestID/rankings", (request: Request, response: Response, next: NextFunction) => rankingsByContest(request, response, next))
  app.get("/api/contests/perks", (request: Request, response: Response, next: NextFunction) => availablePerks(request, response, next))
  app.get("/api/contests/user/:userID", (request: Request, response: Response, next: NextFunction) => myContests(request, response, next))
  app.post("/api/contests/search", (request: Request, response: Response, next: NextFunction) => search(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Retrieves available perks
   */
  def availablePerks(request: Request, response: Response, next: NextFunction): Unit = {
    perksDAO.findAvailablePerks onComplete {
      case Success(perks) => response.send(perks); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves contests by portfolio
   */
  def contestByID(request: Request, response: Response, next: NextFunction): Unit = {
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
          case Failure(e) =>
            response.internalServerError(e); next()
        }
    }
  }

  def exposureChart(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID, chart) = (request.params("id"), request.params("userID"), request.params("chart"))
    positionDAO.findExposure(contestID, userID, chart) onComplete {
      case Success(data) => response.send(data); next()
      case Failure(e) => e.printStackTrace(); response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves contests by userID
   */
  def myContests(request: Request, response: Response, next: NextFunction): Unit = {
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
  def rankingsByContest(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("contestID")
    val outcome = for {
      rankings <- contestDAO.findRankings(contestID)

      // sort the rankings and add the position (e.g. "1st")
      sortedRankings = {
        val myRankings = rankings.sortBy(-_.gainLoss.getOrElse(0.0))
        myRankings.zipWithIndex foreach { case (ranking, index) =>
          ranking.rank = (index + 1) nth
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
