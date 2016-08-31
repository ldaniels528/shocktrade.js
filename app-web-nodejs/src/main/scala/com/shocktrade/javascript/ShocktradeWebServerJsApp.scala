package com.shocktrade.javascript

import com.shocktrade.javascript.routes._
import com.shocktrade.javascript.util.StringHelper._
import org.scalajs.nodejs._
import org.scalajs.nodejs.bodyparser._
import org.scalajs.nodejs.express.fileupload.ExpressFileUpload
import org.scalajs.nodejs.express.{Express, Request, Response}
import org.scalajs.nodejs.expressws.{ExpressWS, WsRouterExtensions}
import org.scalajs.nodejs.globals._
import org.scalajs.nodejs.mongodb.MongoDB
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * ShockTrade Server JavaScript Application
  * @author lawrence.daniels@gmail.com
  */
@JSExportAll
object ShocktradeWebServerJsApp extends js.JSApp {

  override def main() {}

  def startServer(implicit bootstrap: Bootstrap) {
    implicit val require = bootstrap.require

    // determine the port to listen on
    val startTime = System.currentTimeMillis()
    val port = (process.env.get("port") ?? process.env.get("PORT")) getOrElse "1337"
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
    app.use("/assets", express.static("public"))
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
    console.log("Connecting to '%s'...", connectionString)
    val dbFuture = mongo.MongoClient.connectFuture(connectionString)

    // setup routes
    ContestRoutes.init(app, dbFuture)
    ExploreRoutes.init(app, dbFuture)
    NewsRoutes.init(app, dbFuture)
    OnlineStatusRoutes.init(app, dbFuture)
    ProfileRoutes.init(app, dbFuture)
    QuoteRoutes.init(app, dbFuture)
    ResearchRoutes.init(app, dbFuture)
    TradingClockRoutes.init(app, dbFuture)

    // start the listener
    app.listen(port, () => console.log("Server now listening on port %s [%d msec]", port, System.currentTimeMillis() - startTime))
    ()
  }

}
