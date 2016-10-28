package com.shocktrade.webapp

import com.shocktrade.common.util.StringHelper._
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.common.ProcessHelper._
import com.shocktrade.webapp.routes._
import org.scalajs.nodejs._
import org.scalajs.nodejs.bodyparser._
import org.scalajs.nodejs.express.fileupload.ExpressFileUpload
import org.scalajs.nodejs.express.{Express, Request, Response}
import org.scalajs.nodejs.expressws.{ExpressWS, WebSocket, WsRouterExtensions}
import org.scalajs.nodejs.globals.process
import org.scalajs.nodejs.mongodb.{Db, MongoDB}
import org.scalajs.npm.nib.Nib
import org.scalajs.npm.stylus.{MiddlewareOptions, Stylus}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * ShockTrade Server JavaScript Application
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@JSExportAll
object WebServerJsApp extends js.JSApp {
  private val logger = LoggerFactory.getLogger(getClass)

  override def main() {}

  def startServer(implicit bootstrap: Bootstrap) {
    implicit val require = bootstrap.require

    logger.info("Starting the ShockTrade Web Server...")

    // determine the port to listen on
    val startTime = js.Date.now()

    // setup mongodb connection
    logger.info("Loading MongoDB module...")
    implicit val mongo = MongoDB()
    val dbConnect = process.dbConnect getOrElse "mongodb://localhost:27017/shocktrade"
    logger.info("Connecting to database '%s'...", dbConnect)
    implicit val dbFuture = mongo.MongoClient.connectFuture(dbConnect)

    // setup the application
    val port = process.port getOrElse "1337"
    val app = configureApplication()
    app.listen(port, () => logger.info("Server now listening on port %s [%d msec]", port, js.Date.now() - startTime))

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      logger.error("An uncaught exception was fired:")
      logger.error(err.stack)
    }
  }

  def configureApplication()(implicit bootstrap: Bootstrap, require: NodeRequire, dbFuture: Future[Db], mongo: MongoDB) = {
    logger.info("Loading Express modules...")
    implicit val express = Express()
    implicit val app = express().withWsRouting
    implicit val wss = ExpressWS(app)
    implicit val fileUpload = ExpressFileUpload()

    // setup the routes for serving static files
    logger.info("Setting up the routes for serving static files...")
    app.use(fileUpload())
    app.use(express.static("public"))
    app.use("/bower_components", express.static("bower_components"))

    // setup the body parsers
    logger.info("Loading Body Parser...")
    implicit val bodyParser = BodyParser()
    app.use(bodyParser.json())
      .use(bodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))

    // setup stylus & nib for CSS3
    logger.info("Loading Stylus and Nib modules...")
    implicit val stylus = Stylus()
    implicit val nib = Nib()
    app.use(stylus.middleware(new MiddlewareOptions(src = "public", compile = (str: String, file: String) => {
      stylus(str)
        .set("filename", file)
        .use(nib())
    })))

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
    app.ws("/websocket", callback = (ws: WebSocket, request: Request) => {
      ws.onMessage(WebSocketHandler.messageHandler(ws, request, _))
    })

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