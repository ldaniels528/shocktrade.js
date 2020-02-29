package com.shocktrade.webapp

import com.shocktrade.common.util.StringHelper._
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.common.ProcessHelper._
import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.routes.account.{AuthenticationRoutes, UserAccountRoutes, UserProfileRoutes, UserRoutes}
import com.shocktrade.webapp.routes.contest._
import com.shocktrade.webapp.routes.discover._
import com.shocktrade.webapp.routes.research.ResearchRoutes
import com.shocktrade.webapp.routes.social.{OnlineStatusRoutes, PostAttachmentRoutes, PostRoutes}
import io.scalajs.nodejs._
import io.scalajs.npm.bodyparser._
import io.scalajs.npm.express.fileupload.ExpressFileUpload
import io.scalajs.npm.express.{Application, Express, Request, Response}
import io.scalajs.npm.expressws.{ExpressWS, WebSocket, WsInstance, WsRouterExtensions, WsRouting}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
  * ShockTrade Server JavaScript Application
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object WebServerJsApp {
  private val logger = LoggerFactory.getLogger(getClass)

  @JSExport
  def main(args: Array[String]): Unit = {
    logger.info("Starting the ShockTrade Web Server...")

    // determine the port to listen on
    val startTime = js.Date.now()

    // setup the application
    val port = process.port getOrElse "9000"
    val app = configureApplication()
    app.listen(port, () => logger.info("Server now listening on port %s [%d msec]", port, js.Date.now() - startTime))

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      logger.error("An uncaught exception was fired:")
      logger.error(err.stack)
      ()
    }
  }

  /**
   * Creates a new application
   * @return the [[Application]]
   */
  def configureApplication(): Application with WsRouting = {
    logger.info("Loading Express modules...")
    implicit val app: Application with WsRouting = Express().withWsRouting
    implicit val wss: WsInstance = ExpressWS(app)

    // disable caching
    app.disable("etag")

    // setup the routes for serving static files
    logger.info("Setting up middleware...")
    app.use(Express.static("public"))
    app.use("/bower_components", Express.static("bower_components"))
    app.use(BodyParser.json()).use(BodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))
    app.use(ExpressFileUpload())

    // setup logging of the request - response cycles
    app.use((request: Request, response: Response, next: NextFunction) => {
      val startTime = js.Date.now()
      next()
      response.onFinish(() => {
        val elapsedTime = js.Date.now() - startTime
        val query = if (request.query.nonEmpty) (request.query map { case (k, v) => s"$k=$v" } mkString ",").limitTo(120) else "..."
        logger.info("[node] application - %s %s (%s) ~> %d [%d ms]", request.method, request.path, query, response.statusCode, elapsedTime)
      })
    })

    // setup web socket routes
    logger.info("Setting up web socket...")
    app.ws("/websocket", (ws: WebSocket, request: Request) => ws.onMessage(WebSocketHandler.messageHandler(ws, request, _)))

    // setup all other routes
    logger.info("Setting up all other routes...")
    new AuthenticationRoutes(app)
    new ChartRoutes(app)
    new ChatRoutes(app)
    new ContestRoutes(app)
    new ExploreRoutes(app)
    new GlobalSearchRoutes(app)
    new NewsRoutes(app)
    new OnlineStatusRoutes(app)
    new OrderRoutes(app)
    new PortfolioRoutes(app)
    new PostAttachmentRoutes(app)
    new PostRoutes(app)
    new QuoteRoutes(app)
    new RemoteEventRoutes(app)
    new ResearchRoutes(app)
    new TradingClockRoutes(app)
    new UserAccountRoutes(app)
    new UserProfileRoutes(app)
    new UserRoutes(app)
    app
  }

}