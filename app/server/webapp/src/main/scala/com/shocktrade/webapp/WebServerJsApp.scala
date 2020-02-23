package com.shocktrade.webapp

import com.shocktrade.common.util.StringHelper._
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.common.ProcessHelper._
import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.routes.account.{UserAccountRoutes, AuthenticationRoutes, UserProfileRoutes, UserRoutes}
import com.shocktrade.webapp.routes.contest.{ChartRoutes, ChatRoutes, ContestRoutes, PortfolioRoutes}
import com.shocktrade.webapp.routes.explore.ExploreRoutes
import com.shocktrade.webapp.routes.news.NewsRoutes
import com.shocktrade.webapp.routes.research.ResearchRoutes
import io.scalajs.nodejs._
import io.scalajs.npm.bodyparser._
import io.scalajs.npm.express.fileupload.ExpressFileUpload
import io.scalajs.npm.express.{Application, Express, Request, Response}
import io.scalajs.npm.expressws.{ExpressWS, WebSocket, WsInstance, WsRouterExtensions, WsRouting}
import io.scalajs.npm.mongodb.{Db, MongoClient}

import scala.concurrent.Future
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

    // setup mongodb connection
    val dbConnect = "mongodb://dev001:27017/shocktrade"
    logger.info(s"Connecting to database '$dbConnect'...")
    implicit val dbFuture: Future[Db] = MongoClient.connectFuture(dbConnect)

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
  def configureApplication()(implicit dbFuture: Future[Db]): Application with WsRouting = {
    logger.info("Loading Express modules...")
    implicit val app: Application with WsRouting = Express().withWsRouting
    implicit val wss: WsInstance = ExpressWS(app)

    // setup the routes for serving static files
    logger.info("Setting up the routes for serving static files...")
    app.use(ExpressFileUpload())
    app.use(Express.static("public"))
    app.use("/bower_components", Express.static("bower_components"))

    // setup the body parsers
    logger.info("Loading Body Parser...")
    app.use(BodyParser.json())
      .use(BodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))

    // disable caching
    app.disable("etag")

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
    new ChartRoutes(app, dbFuture)
    new ChatRoutes(app)
    new ContestRoutes(app, dbFuture)
    new ExploreRoutes(app)
    new NewsRoutes(app)
    new OnlineStatusRoutes(app, dbFuture)
    new PortfolioRoutes(app, dbFuture)
    new PostRoutes(app, dbFuture)
    new UserProfileRoutes(app, dbFuture)
    new RemoteEventRoutes(app, dbFuture)
    new QuoteRoutes(app, dbFuture)
    new ResearchRoutes(app)
    new SearchRoutes(app, dbFuture)
    new TradingClockRoutes(app, dbFuture)
    new UserAccountRoutes(app)
    new UserRoutes(app, dbFuture)
    app
  }

}