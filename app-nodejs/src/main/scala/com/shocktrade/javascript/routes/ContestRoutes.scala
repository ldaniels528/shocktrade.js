package com.shocktrade.javascript.routes

import com.shocktrade.javascript.data.ContestDAO._
import com.shocktrade.javascript.data.ContestData._
import com.shocktrade.javascript.data.{ContestDAO, ContestData}
import com.shocktrade.javascript.forms.{ContestCreateForm, ContestSearchForm}
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}
import org.scalajs.nodejs.{NodeRequire, console}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * Contest Routes
  * @author lawrence.daniels@gmail.com
  */
object ContestRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) = {
    implicit val contestDAO = dbFuture.flatMap(_.getContestDAO)

    // individual contests
    app.post("/api/contest", (request: Request, response: Response, next: NextFunction) => search(request, response, next))

    // collections of contests
    app.get("/api/contests/player/:playerID/totalInvestment", (request: Request, response: Response, next: NextFunction) => totalInvestment(request, response, next))
    app.post("/api/contests/search", (request: Request, response: Response, next: NextFunction) => search(request, response, next))
  }

  def create(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, contestDAO: Future[ContestDAO], mongo: MongoDB) = {
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

  def search(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, contestDAO: Future[ContestDAO], mongo: MongoDB) = {
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

  def totalInvestment(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, contestDAO: Future[ContestDAO], mongo: MongoDB) = {
    val playerID = request.params("playerID")
    contestDAO.flatMap(_.totalInvestment(playerID)) onComplete {
      case Success(playerData) => response.send(playerData); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  @ScalaJSDefined
  class ValidationErrors(val messages: js.Array[String]) extends js.Object

}
