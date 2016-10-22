package com.shocktrade.webapp.routes

import com.shocktrade.server.dao.users.ProfileDAO._
import com.shocktrade.common.models.user.ProfileLike.QuoteFilter
import com.shocktrade.server.dao.users.ProfileData
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}
import org.scalajs.nodejs.{NodeRequire, console}

import scala.concurrent.{ExecutionContext, Future}
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
    implicit val profileDAO = dbFuture.flatMap(_.getProfileDAO)

    app.get("/api/profile/facebook/:fbID", (request: Request, response: Response, next: NextFunction) => profileByFBID(request, response, next))
    app.post("/api/profile/facebook", (request: Request, response: Response, next: NextFunction) => profileByFacebook(request, response, next))
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
            case Success(result) if result.isOk =>
              console.log("result => %j", result)
              response.send(result.value);
              next()
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

}