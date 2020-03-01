package com.shocktrade.webapp.routes
package contest

import com.shocktrade.common.forms.{ContestCreationForm, ContestSearchForm, ValidationErrors}
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Contest Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val contestDAO = ContestDAO()
  private val perksDAO = PerksDAO()

  // individual contests
  app.get("/api/contest/:id", (request: Request, response: Response, next: NextFunction) => contestByID(request, response, next))
  app.post("/api/contest", (request: Request, response: Response, next: NextFunction) => createContest(request, response, next))

  // collections of contests
  app.get("/api/contests/perks", (request: Request, response: Response, next: NextFunction) => availablePerks(request, response, next))
  app.get("/api/contests/user/:userID", (request: Request, response: Response, next: NextFunction) => contestsByUser(request, response, next))
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

  /**
   * Retrieves contests by userID
   */
  def contestsByUser(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    val outcome = for {
      contests <- contestDAO.findByUser(userID)
      sortedContests = contests.sortBy(-_.gainLoss.getOrElse(0.0))
      rankings = sortedContests.zipWithIndex map { case (ranking, rank) =>
        ranking.rank = rankOf(rank + 1)
        ranking
      }
    } yield rankings

    outcome onComplete {
      case Success(contests) => response.send(contests); next()
      case Failure(e) => response.internalServerError(e); next()
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
          case Failure(e) => response.internalServerError(e); next()
        }
      case messages =>
        response.badRequest(new ValidationErrors(messages)); next()
    }
  }

  private def rankOf(rank: Int): String = {
    val suffix = rank.toString match {
      case n if n.endsWith("11") => "th"
      case n if n.endsWith("1") => "st"
      case n if n.endsWith("2") => "nd"
      case n if n.endsWith("3") => "rd"
      case _ => "th"
    }
    s"$rank$suffix"
  }

}
