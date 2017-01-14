package com.shocktrade.webapp.routes

import com.shocktrade.common.models.FacebookAppInfo
import com.shocktrade.server.common.LoggerFactory
import io.scalajs.nodejs.os.OS
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.Db

import scala.concurrent.{ExecutionContext, Future}

/**
  * Social Networking Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SocialRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext) {
    val logger = LoggerFactory.getLogger(getClass)

    // load the modules
    val hostname = OS.hostname()

    // API URI
    app.get("/api/social/facebook", (request: Request, response: Response, next: NextFunction) => facebookAppID(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def facebookAppID(request: Request, response: Response, next: NextFunction) = {
      logger.info(s"(Facebook App) Hostname is '$hostname'...")
      val appId = hostname match {
        case s if s.endsWith("shocktrade.biz") => "616941558381179"
        case s if s.endsWith("shocktrade.com") => "364507947024983"
        case s if s.endsWith("shocktrade.net") => "616569495084446"
        case host =>
          logger.warn(s"Unrecognized hostname '$host'")
          "522523074535098" // unknown, so local dev
      }

      response.send(new FacebookAppInfo(appId))
      next()
    }

  }

}
