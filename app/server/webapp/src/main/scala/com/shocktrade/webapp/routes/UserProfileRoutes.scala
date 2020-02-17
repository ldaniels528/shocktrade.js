package com.shocktrade.webapp.routes

import com.shocktrade.common.models.user.NetWorth
import com.shocktrade.server.dao.contest.PortfolioDAO._
import com.shocktrade.server.dao.securities.QtyQuote
import com.shocktrade.server.dao.securities.SecuritiesDAO._
import com.shocktrade.server.dao.users.ProfileDAO._
import com.shocktrade.server.dao.users.{UserDAO, UserProfileData}
import com.shocktrade.server.facade.PricingQuote
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.Db
import io.scalajs.util.OptionHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * User Profile Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object UserProfileRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext) {
    val portfolioDAO = dbFuture.map(_.getPortfolioDAO)
    val profileDAO = dbFuture.map(_.getProfileDAO)
    val securitiesDAO = dbFuture.map(_.getSecuritiesDAO)
    val userDAO = UserDAO()

    app.get("/api/profile/facebook/:fbID", (request: Request, response: Response, next: NextFunction) => profileByFBID(request, response, next))
    app.post("/api/profile/facebook", (request: Request, response: Response, next: NextFunction) => profileByFacebook(request, response, next))
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
        quotes <- securitiesDAO.flatMap(_.findQuotesBySymbols[PricingQuote](symbols, fields = PricingQuote.Fields))
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

    def profileByFacebook(request: Request, response: Response, next: NextFunction): Unit = {
      val fbProfile = request.bodyAs[FBProfile]
      val form = for {
        fbId <- fbProfile.id
        name <- fbProfile.name
      } yield {
        new UserProfileData(
          _id = js.undefined,
          facebookID = fbId,
          name = name,
          country = "US",
          level = 1,
          rep = 1,
          netWorth = 250000.00,
          wallet = 250000.00,
          totalXP = 0,
          favoriteSymbols = js.Array("AAPL", "MSFT"),
          recentSymbols = js.Array("AAPL", "MSFT"),
          friends = js.Array[String](),
          awards = js.Array[String](),
          followers = js.Array[String](),
          lastLoginTime = new js.Date())
      }

      form.toOption match {
        case Some(newProfile) =>
          profileDAO.flatMap(_.findOneOrCreateByFacebook(newProfile, newProfile.facebookID.orNull).toFuture) onComplete {
            case Success(result) if result.isOk => response.send(result.value); next()
            case Success(_) => response.badRequest("User could not be created"); next()
            case Failure(e) =>
              e.printStackTrace()
              response.internalServerError(e)
              next()
          }
        case None => response.badRequest("Invalid form"); next()
      }
    }

    def profileByFBID(request: Request, response: Response, next: NextFunction): Unit = {
      val fbId = request.params.apply("fbID")
      profileDAO.flatMap(_.findOneByFacebookID(fbId)) onComplete {
        case Success(Some(profile)) => response.send(profile); next()
        case Success(None) => response.notFound(fbId); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

  }

  class FBProfile(val id: js.UndefOr[String], val name: js.UndefOr[String]) extends js.Object

  class UserInfo(val wallet: Double) extends js.Object

}