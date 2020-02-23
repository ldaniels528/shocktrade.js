package com.shocktrade.webapp.routes.account

import com.shocktrade.common.models.user.NetWorth
import com.shocktrade.server.dao.contest.PortfolioDAO._
import com.shocktrade.server.dao.securities.QtyQuote
import com.shocktrade.server.dao.users.ProfileDAO._
import com.shocktrade.server.dao.users.{UserDAO, UserProfileData}
import com.shocktrade.webapp.routes.NextFunction
import com.shocktrade.webapp.routes.explore.StockQuoteDAO
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.Db
import io.scalajs.util.OptionHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
 * User Profile Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserProfileRoutes(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext) {
  private val portfolioDAO = dbFuture.map(_.getPortfolioDAO)
  private val profileDAO = dbFuture.map(_.getProfileDAO)
  private val stockQuoteDAO = StockQuoteDAO()
  private val userDAO = UserDAO()

  app.get("/api/profile/:userID/netWorth", (request: Request, response: Response, next: NextFunction) => netWorth(request, response, next))
  app.put("/api/profile/:userID/recent/:symbol", (request: Request, response: Response, next: NextFunction) => addRecentSymbol(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def addRecentSymbol(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params.apply("userID")
    val symbol = request.params.apply("symbol")
    profileDAO.flatMap(_.addRecentSymbol(userID, symbol).toFuture) onComplete {
      case Success(result) if result.isOk =>
        result.valueAs[UserProfileData] match {
          case Some(profile) => response.send(profile); next()
          case None => response.notFound(userID)
        }
      case Success(result) => response.badRequest(result)
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def netWorth(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params.apply("userID")
    val outcome = for {
      user <- userDAO.findByID(userID).map(_.orDie("User not found"))
      portfolios <- portfolioDAO.flatMap(_.findByPlayer(userID))

      wallet = user.wallet getOrElse 0d
      cashFunds = portfolios.flatMap(_.cashAccount.toOption).flatMap(_.funds.toOption).sum
      marginFunds = portfolios.flatMap(_.marginAccount.toOption).flatMap(_.funds.toOption).sum

      positions = portfolios.flatMap(_.positions.toOption).flatten
      symbolQtys = positions.flatMap(p => (for {symbol <- p.symbol; qty <- p.quantity} yield QtyQuote(symbol, qty)).toOption)
      symbols = symbolQtys.map(_.symbol).distinct
      quotes <- stockQuoteDAO.findQuotes(symbols)
      quoteMap = Map(quotes.map(q => q.symbol -> q): _*)
      investments = (symbolQtys map { case QtyQuote(symbol, qty) => quoteMap.get(symbol).flatMap(_.lastTrade.toOption).orZero * qty }).sum
      netWorth = wallet + investments + cashFunds + marginFunds

      // update the user's networth
      w <- profileDAO.flatMap(_.updateNetWorth(userID, netWorth).toFuture)
    } yield netWorth

    outcome onComplete {
      case Success(total) => response.send(new NetWorth(total))
      case Failure(e) =>
        response.internalServerError(e); next()
    }
  }

}
