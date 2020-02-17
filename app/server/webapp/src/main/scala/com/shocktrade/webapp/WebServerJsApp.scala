package com.shocktrade.webapp

import com.shocktrade.common.util.StringHelper._
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.common.ProcessHelper._
import com.shocktrade.webapp.routes._
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
    logger.info("Loading MongoDB module...")
    val dbConnect = "mongodb://dev001:27017/shocktrade"
    logger.info("Connecting to database '%s'...", dbConnect)
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
    ChartRoutes.init(app, dbFuture)
    ChatRoutes.init(app, dbFuture)
    ContestRoutes.init(app, dbFuture)
    ExploreRoutes.init(app, dbFuture)
    NewsRoutes.init(app, dbFuture)
    OnlineStatusRoutes.init(app, dbFuture)
    PortfolioRoutes.init(app, dbFuture)
    PostRoutes.init(app, dbFuture)
    UserProfileRoutes.init(app, dbFuture)
    RemoteEventRoutes.init(app, dbFuture)
    QuoteRoutes.init(app, dbFuture)
    ResearchRoutes.init(app, dbFuture)
    SearchRoutes.init(app, dbFuture)
    SocialRoutes.init(app, dbFuture)
    TradingClockRoutes.init(app, dbFuture)
    UserRoutes.init(app, dbFuture)
    app
  }

}