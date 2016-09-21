package com.shocktrade.webapp

import com.shocktrade.util.StringHelper._
import com.shocktrade.webapp.routes._
import org.scalajs.nodejs.bodyparser._
import org.scalajs.nodejs.express.fileupload.ExpressFileUpload
import org.scalajs.nodejs.express.{Express, Request, Response}
import org.scalajs.nodejs.expressws.{ExpressWS, WebSocket, WsRouterExtensions}
import org.scalajs.nodejs.globals._
import org.scalajs.nodejs.mongodb.MongoDB
import org.scalajs.nodejs.{console, _}
import org.scalajs.sjs.OptionHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * ShockTrade Server JavaScript Application
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@JSExportAll
object WebServerJsApp extends js.JSApp {

  override def main() {}

  def startServer(implicit bootstrap: Bootstrap) {
    implicit val require = bootstrap.require

    console.log("Starting the Shocktrade Web Server...")

    // determine the port to listen on
    val startTime = System.currentTimeMillis()

    // get the web application port
    val port = (process.env.get("port") ?? process.env.get("PORT")) getOrElse "1337"

    // determine the database connection URL
    val connectionString = process.env.get("db_connection") getOrElse "mongodb://localhost:27017/shocktrade"

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      console.error("An uncaught exception was fired:")
      console.error(err.stack)
    }

    console.log("Loading Express modules...")
    implicit val express = Express()
    implicit val app = express().withWsRouting
    implicit val wss = ExpressWS(app)
    implicit val fileUpload = ExpressFileUpload()

    console.log("Loading MongoDB module...")
    implicit val mongo = MongoDB()

    // setup the body parsers
    console.log("Setting up body parsers...")
    val bodyParser = BodyParser()
    app.use(bodyParser.json())
    app.use(bodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))

    // setup the routes for serving static files
    console.log("Setting up the routes for serving static files...")
    app.use(fileUpload())
    app.use(express.static("public"))
    app.use("/bower_components", express.static("bower_components"))

    // disable caching
    app.disable("etag")

    // setup logging of the request - response cycles
    app.use((request: Request, response: Response, next: NextFunction) => {
      val startTime = System.currentTimeMillis()
      next()
      response.onFinish(() => {
        val elapsedTime = System.currentTimeMillis() - startTime
        val query = if (request.query.nonEmpty) (request.query map { case (k, v) => s"$k=$v" } mkString ",").limitTo(120) else "..."
        console.log("[node] application - %s %s (%s) ~> %d [%d ms]", request.method, request.path, query, response.statusCode, elapsedTime)
      })
    })

    // setup mongodb connection
    console.log("Connecting to database '%s'...", connectionString)
    implicit val dbFuture = mongo.MongoClient.connectFuture(connectionString)

    // setup web socket routes
    app.ws("/websocket", callback = (ws: WebSocket, request: Request) => {
      ws.onMessage(WebSocketHandler.messageHandler(ws, request, _))
    })

    // setup all other routes
    ChartRoutes.init(app, dbFuture)
    ChatRoutes.init(app, dbFuture)
    ContestRoutes.init(app, dbFuture)
    ExploreRoutes.init(app, dbFuture)
    NewsRoutes.init(app, dbFuture)
    OnlineStatusRoutes.init(app, dbFuture)
    PortfolioRoutes.init(app, dbFuture)
    ProfileRoutes.init(app, dbFuture)
    RemoteEventRoutes.init(app, dbFuture)
    QuoteRoutes.init(app, dbFuture)
    ResearchRoutes.init(app, dbFuture)
    TradingClockRoutes.init(app, dbFuture)

    // start the listener
    app.listen(port, () => console.log("Server now listening on port %s [%d msec]", port, System.currentTimeMillis() - startTime))
    ()
  }

}
