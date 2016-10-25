package com.shocktrade.webapp.routes

import com.shocktrade.common.models.quote.PricingQuote
import com.shocktrade.common.models.user.ProfileLike.QuoteFilter
import com.shocktrade.server.dao.contest.PortfolioDAO._
import com.shocktrade.server.dao.securities.QtyQuote
import com.shocktrade.server.dao.securities.SecuritiesDAO._
import com.shocktrade.server.dao.users.ProfileDAO._
import com.shocktrade.server.dao.users.ProfileData
import com.shocktrade.server.dao.users.UserDAO._
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}
import org.scalajs.nodejs.{NodeRequire, console}
import org.scalajs.sjs.OptionHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * Profile Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ProfileRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) = {
    val portfolioDAO = dbFuture.flatMap(_.getPortfolioDAO)
    val profileDAO = dbFuture.flatMap(_.getProfileDAO)
    val securitiesDAO = dbFuture.flatMap(_.getSecuritiesDAO)
    val userDAO = dbFuture.flatMap(_.getUserDAO)

    app.get("/api/profile/facebook/:fbID", (request: Request, response: Response, next: NextFunction) => profileByFBID(request, response, next))
    app.post("/api/profile/facebook", (request: Request, response: Response, next: NextFunction) => profileByFacebook(request, response, next))
    app.get("/api/profile/:userID/netWorth", (request: Request, response: Response, next: NextFunction) => netWorth(request, response, next))
    app.put("/api/profile/:userID/recent/:symbol", (request: Request, response: Response, next: NextFunction) => addRecentSymbol(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def addRecentSymbol(request: Request, response: Response, next: NextFunction) = {
      val userID = request.params("userID")
      val symbol = request.params("symbol")
      profileDAO.flatMap(_.addRecentSymbol(userID, symbol).toFuture) onComplete {
        case Success(result) if result.isOk =>
          result.valueAs[ProfileData] match {
            case Some(profile) => response.send(profile); next()
            case None => response.notFound(userID)
          }
        case Success(result) => response.badRequest(result)
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def netWorth(request: Request, response: Response, next: NextFunction) = {
      val userID = request.params("userID")
      val outcome = for {
        user <- userDAO.flatMap(_.findUserWithFields[UserInfo](userID, fields = UserInfo.Fields)).map(_.orDie("User not found"))
        portfolios <- portfolioDAO.flatMap(_.findByPlayer(userID))
        cashFunds = portfolios.flatMap(_.cashAccount.toOption).flatMap(_.funds.toOption).sum
        marginFunds = portfolios.flatMap(_.marginAccount.toOption).flatMap(_.funds.toOption).sum

        positions = portfolios.flatMap(_.positions.toOption).flatten
        symbolQtys = positions.flatMap(p => (for {symbol <- p.symbol; qty <- p.quantity} yield QtyQuote(symbol, qty)).toOption)
        symbols = symbolQtys.map(_.symbol).distinct
        quotes <- securitiesDAO.flatMap(_.findQuotesBySymbols[PricingQuote](symbols, fields = PricingQuote.Fields))
        quoteMap = Map(quotes.map(q => q.symbol -> q): _*)
        investments = symbolQtys map { case QtyQuote(symbol, qty) => quoteMap.get(symbol).flatMap(_.lastTrade.toOption).orZero * qty } sum
      } yield user.netWorth + investments + cashFunds + marginFunds

      outcome onComplete {
        case Success(total) => response.send(new NetWorth(total))
        case Failure(e) =>
          response.internalServerError(e); next()
      }
    }

    def profileByFacebook(request: Request, response: Response, next: NextFunction) = {
      val fbProfile = request.bodyAs[FBProfile]
      console.log("fbProfile = %j", fbProfile)
      val form = for {
        fbId <- fbProfile.id
        name <- fbProfile.name
      } yield {
        new ProfileData(
          facebookID = fbId,
          name = name,
          country = "US",
          level = 1,
          rep = 1,
          netWorth = 250000.00,
          totalXP = 0,
          favoriteSymbols = js.Array("AAPL", "MSFT"),
          recentSymbols = js.Array("AAPL", "MSFT"),
          filters = js.Array[QuoteFilter](),
          friends = js.Array[String](),
          accomplishments = js.Array[String](),
          acquaintances = js.Array[String](),
          lastLoginTime = new js.Date())
      }

      form.toOption match {
        case Some(newProfile) =>
          profileDAO.flatMap(_.findOneOrCreateByFacebook(newProfile, newProfile.facebookID.orNull).toFuture) onComplete {
            case Success(result) if result.isOk => response.send(result.value); next()
            case Success(result) => response.badRequest("User could not be created"); next()
            case Failure(e) =>
              e.printStackTrace()
              response.internalServerError(e)
              next()
          }
        case None => response.badRequest("Invalid form"); next()
      }
    }

    def profileByFBID(request: Request, response: Response, next: NextFunction) = {
      val fbId = request.params("fbID")
      profileDAO.flatMap(_.findOneByFacebookID(fbId)) onComplete {
        case Success(Some(profile)) => response.send(profile); next()
        case Success(None) => response.notFound(fbId); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

  }

  @ScalaJSDefined
  class FBProfile(val id: js.UndefOr[String], val name: js.UndefOr[String]) extends js.Object

  @ScalaJSDefined
  class NetWorth(val value: Double) extends js.Object

  @ScalaJSDefined
  trait UserInfo extends js.Object {
    def netWorth: Double
  }

  object UserInfo {
    val Fields = js.Array("netWorth")
  }

}