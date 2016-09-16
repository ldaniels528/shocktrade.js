package com.shocktrade.webapp.routes

import com.shocktrade.common.dao.contest.ContestDAO._
import com.shocktrade.common.dao.contest.ContestData
import com.shocktrade.common.dao.contest.ContestData._
import com.shocktrade.common.dao.contest.PerksDAO._
import com.shocktrade.common.forms.{ContestCreateForm, ContestSearchForm}
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}
import org.scalajs.nodejs.{NodeRequire, console}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * Contest Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ContestRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) = {
    implicit val contestDAO = dbFuture.flatMap(_.getContestDAO)
    implicit val perksDAO = dbFuture.flatMap(_.getPerksDAO)

    // individual contests
    app.get("/api/contest/:id", (request: Request, response: Response, next: NextFunction) => contestByID(request, response, next))
    app.post("/api/contest", (request: Request, response: Response, next: NextFunction) => createContest(request, response, next))

    // collections of contests
    app.get("/api/contests/perks", (request: Request, response: Response, next: NextFunction) => availablePerks(request, response, next))
    app.get("/api/contests/player/:playerID", (request: Request, response: Response, next: NextFunction) => contestsByPlayer(request, response, next))
    app.post("/api/contests/search", (request: Request, response: Response, next: NextFunction) => search(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def availablePerks(request: Request, response: Response, next: NextFunction) = {
      perksDAO.flatMap(_.findAvailablePerks) onComplete {
        case Success(perks) => response.send(perks); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves contests by player
      */
    def contestByID(request: Request, response: Response, next: NextFunction) = {
      val contestID = request.params("id")
      contestDAO.flatMap(_.findOneByID(contestID)) onComplete {
        case Success(Some(contest)) => response.send(contest); next()
        case Success(None) => response.notFound(contestID); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Creates a new contest
      */
    def createContest(request: Request, response: Response, next: NextFunction) = {
      val form = request.bodyAs[ContestCreateForm]
      form.validate match {
        case messages if messages.isEmpty =>
          val contest = form.toContest
          contestDAO.flatMap(_.create(contest).toFuture) onComplete {
            case Success(result) if result.isOk => response.send(result.opsAs[ContestData]); next()
            case Success(result) =>
              console.log("result = %j", result)
              response.badRequest("Contest could not be created")
              next()
            case Failure(e) => response.internalServerError(e); next()
          }
        case messages =>
          response.badRequest(new ValidationErrors(messages)); next()
      }
    }

    /**
      * Retrieves contests by player
      */
    def contestsByPlayer(request: Request, response: Response, next: NextFunction) = {
      val playerID = request.params("playerID")
      contestDAO.flatMap(_.findByPlayer(playerID)) onComplete {
        case Success(contests) => response.send(contests); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Searches for contest via a [[ContestSearchForm contest search form]]
      */
    def search(request: Request, response: Response, next: NextFunction) = {
      val form = request.bodyAs[ContestSearchForm]
      form.validate match {
        case messages if messages.isEmpty =>
          contestDAO.flatMap(_.search(form)) onComplete {
            case Success(contests) => response.send(contests); next()
            case Failure(e) => response.internalServerError(e); next()
          }
        case messages =>
          response.badRequest(new ValidationErrors(messages)); next()
      }
    }

  }

  @ScalaJSDefined
  class ValidationErrors(val messages: js.Array[String]) extends js.Object

}
