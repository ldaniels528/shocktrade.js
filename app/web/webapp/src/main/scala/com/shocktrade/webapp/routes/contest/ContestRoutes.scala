package com.shocktrade.webapp.routes
package contest

import com.shocktrade.common.forms.{ContestCreateForm, ContestSearchForm}
import com.shocktrade.common.models.contest._
import com.shocktrade.webapp.routes.account.{UserAccountDAO, UserDAO}
import com.shocktrade.webapp.routes.contest.ContestRoutes._
import io.scalajs.JSON
import io.scalajs.nodejs.console
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.util.DateHelper._
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Contest Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val contestDAO = ContestDAO()
  private val chatDAO = ChatDAO()
  private val portfolioDAO = PortfolioDAO()
  private val perksDAO = PerksDAO()
  private val userDAO = UserDAO()
  private val userAccountDAO = UserAccountDAO()

  // individual contests
  app.get("/api/contest/:id", (request: Request, response: Response, next: NextFunction) => contestByID(request, response, next))
  app.post("/api/contest", (request: Request, response: Response, next: NextFunction) => createContest(request, response, next))

  // collections of contests
  app.get("/api/contests/perks", (request: Request, response: Response, next: NextFunction) => availablePerks(request, response, next))
  app.get("/api/contests/user/:userID", (request: Request, response: Response, next: NextFunction) => contestsByPlayer(request, response, next))
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
    val form = request.bodyAs[ContestCreateForm]
    val args = (for {
      userID <- form.userID
      startingBalance <- form.startingBalance.flatMap(_.value)
    } yield (userID, startingBalance)).toOption
    console.log(s"form = ${JSON.stringify(form)}")

    (args, form.validate) match {
      case (Some((userID, startingBalance)), messages) if messages.isEmpty =>
        val (contest, portfolio) = toRecords(form)
        val outcome = for {
          // lookup the user
          user <- userDAO.findByID(userID)
            .map(_.getOrElse(throw new IllegalStateException(s"User $userID not found")))

          // create & retrieve the contest
          w0 <- contestDAO.create(contest) if w0 == 1
          newContest <- contestDAO.findOneByName(contest.name.orNull)
            .map(_.getOrElse(throw new IllegalStateException("Contest not found")))

          // capture the contestID
          contestID = newContest.contestID.getOrElse(throw new IllegalStateException("Contest ID not found"))

          // create & retrieve the portfolio
          w1 <- portfolioDAO.create(portfolio) if w1 == 1
          newPortfolio <- portfolioDAO.findOneByContest(contestID = contestID, userID = userID)
            .map(_.getOrElse(throw new IllegalStateException("Portfolio not found")))

          // capture the portfolioID
          portfolioID = newPortfolio.portfolioID.getOrElse(throw new IllegalStateException("Portfolio ID not found"))

          // deduct the funds for the contest
          w2 <- userAccountDAO.deductFunds(userID, startingBalance) if w2 == 1

          // create the welcome message
          w3 <- chatDAO.addChatMessage(contestID = contestID, portfolioID = portfolioID, message = s"Welcome to ${form.name}") if w3 == 1
        } yield (user, newContest, newPortfolio)

        outcome onComplete {
          case Success((user, contest, portfolio)) => response.send(new ContestAndPortfolio(contest, portfolio)); next()
          case Success(_) => response.badRequest("Contest could not be created"); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      case (_, messages) =>
        response.badRequest(new ValidationErrors(messages)); next()
    }
  }

  private def toRecords(form: ContestCreateForm): (ContestData, PortfolioData) = {
    val contestRecord = new ContestData(
      contestID = js.undefined,
      name = form.name,
      hostUserID = form.userID,
      startingBalance = form.startingBalance.map(_.value),
      startTime = new js.Date(),
      expirationTime = form.duration.map(_.value.days + new js.Date()),
      status = ContestLike.StatusActive,
      friendsOnly = form.friendsOnly ?? false,
      invitationOnly = form.invitationOnly ?? false,
      levelCap = form.levelCap,
      perksAllowed = form.perksAllowed ?? false,
      robotsAllowed = form.robotsAllowed ?? false
    )
    val portfolioRecord = new PortfolioData(
      contestID = js.undefined,
      portfolioID = js.undefined,
      userID = form.userID,
      funds = form.startingBalance.map(_.value),
      asOfDate = new js.Date(),
      active = true
    )
    (contestRecord, portfolioRecord)
  }

  /**
   * Retrieves contests by userID
   */
  def contestsByPlayer(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    contestDAO.findByUser(userID) onComplete {
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

}

/**
 * Contest Routes Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestRoutes {

  class ContestAndPortfolio(val contest: ContestData, val portfolio: PortfolioData) extends js.Object

  class ValidationErrors(val messages: js.Array[String]) extends js.Object

}