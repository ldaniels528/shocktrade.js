package com.shocktrade.webapp.routes

import com.shocktrade.common.dao.contest.ContestDAO._
import com.shocktrade.common.dao.contest.ContestData._
import com.shocktrade.common.dao.contest.PerksDAO._
import com.shocktrade.common.dao.contest.PortfolioDAO._
import com.shocktrade.common.dao.contest.ProfileDAO._
import com.shocktrade.common.dao.contest.{ContestData, _}
import com.shocktrade.common.forms.{ContestCreateForm, ContestSearchForm}
import com.shocktrade.common.models.contest.{CashAccount, MarginAccount, PerformanceLike}
import org.scalajs.nodejs.{console, _}
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}
import org.scalajs.nodejs.util.ScalaJsHelper._

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
    val contestDAO = dbFuture.flatMap(_.getContestDAO)
    val portfolioDAO = dbFuture.flatMap(_.getPortfolioDAO)
    val profileDAO = dbFuture.flatMap(_.getProfileDAO)
    val perksDAO = dbFuture.flatMap(_.getPerksDAO)

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
      console.log("form = %j", form)
      form.validate match {
        case messages if messages.isEmpty =>
          val contest = form.toContest
          val outcome = for {
            newContestOpt <- createNewContest(contest)
            portfolioOpt <- createNewPortfolio(contest, newContestOpt)
            deducted <- deductEntryFee(newContestOpt)
          } yield (newContestOpt, portfolioOpt, deducted)

          outcome onComplete {
            case Success((Some(newContest), Some(portfolio), true)) => response.send(new ContestAndPortfolio(newContest, portfolio)); next()
            case Success(_) => response.badRequest("Contest could not be created"); next()
            case Failure(e) => response.internalServerError(e); next()
          }
        case messages =>
          response.badRequest(new ValidationErrors(messages)); next()
      }
    }

    def createNewContest(contest: ContestData) = {
      for {
        w0 <- contestDAO.flatMap(_.create(contest).toFuture)
        newContest = w0 match {
          case w if w.isOk => w.opsAs[ContestData].headOption
          case w =>
            console.warn("Failed contest outcome = %j", w)
            die(s"Contest '${contest.name.orNull}' creation failed")
        }
      } yield newContest
    }

    def createNewPortfolio(contest: ContestData, newContestOpt: Option[ContestData]) = {
      for {
        w0 <- newContestOpt match {
          case Some(newContest) => portfolioDAO.flatMap(_.create(newContest.toOwnersPortfolio).toFuture)
          case None => die(s"Owner's portfolio for Contest '${contest.name.orNull}' creation failed")
        }
        portfolioOpt = w0 match {
          case w if w.isOk => w.opsAs[PortfolioData].headOption
          case w =>
            console.warn("Failed portfolio outcome = %j", w)
            die("Failed to create portfolio")
        }
      } yield portfolioOpt
    }

    def deductEntryFee(newContestOpt: Option[ContestData]) = {
      val result = for {
        newContest <- newContestOpt
        userID <- newContest.creator.flatMap(_._id).toOption
        entryFee <- newContest.startingBalance.toOption
      } yield (userID, newContest, entryFee)

      result match {
        case Some((userID, newContest, entryFee)) =>
          profileDAO.flatMap(_.deductFunds(userID, entryFee).toFuture) map {
            case w if w.result.isOk => true
            case w =>
              console.warn("Error deducting entry fee; w => %j", w)
              false
          }
        case None => Future.successful(false)
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
  class ContestAndPortfolio(val contest: ContestData, val portfolio: PortfolioData) extends js.Object

  @ScalaJSDefined
  class ValidationErrors(val messages: js.Array[String]) extends js.Object

  /**
    * Contest Conversion
    * @param contest the given [[ContestData contest]]
    */
  final implicit class ContestConversion(val contest: ContestData) extends AnyVal {

    @inline
    def toOwnersPortfolio = {
      new PortfolioData(
        contestID = contest._id.map(_.toHexString()),
        contestName = contest.name,
        playerID = contest.creator.flatMap(_._id),
        cashAccount = new CashAccount(funds = contest.startingBalance, asOfDate = contest.startTime),
        marginAccount = new MarginAccount(funds = 0.0, asOfDate = contest.startTime, initialMargin = 0.50, interestPaid = 0.00),
        orders = emptyArray[OrderData],
        closedOrders = emptyArray[OrderData],
        positions = emptyArray[PositionData],
        performance = emptyArray[PerformanceLike],
        perks = emptyArray[String])
    }
  }

}
