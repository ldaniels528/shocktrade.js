package com.shocktrade.webapp

import com.shocktrade.common.util.StringHelper._
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.common.ProcessHelper._
import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.routes.account._
import com.shocktrade.webapp.routes.account.dao._
import com.shocktrade.webapp.routes.contest._
import com.shocktrade.webapp.routes.discover._
import com.shocktrade.webapp.routes.research.ResearchRoutes
import com.shocktrade.webapp.routes.robot.{RobotProcessor, RobotRoutes}
import com.shocktrade.webapp.routes.social.{PostAttachmentRoutes, PostRoutes, SocialRoutes}
import io.scalajs.nodejs._
import io.scalajs.npm.bodyparser._
import io.scalajs.npm.express.fileupload.{ExpressFileUpload, FileUploadOptions}
import io.scalajs.npm.express.{Application, Express, Request, Response}
import io.scalajs.npm.expressws.{ExpressWS, WebSocket, WsInstance, WsRouterExtensions, WsRouting}

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

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

    // setup the routes for serving static files
    logger.info("Setting up middleware...")
    app.use(Express.static("public"))
      .use("/bower_components", Express.static("bower_components"))
      .use(BodyParser.json())
      .use(BodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))
      .use(ExpressFileUpload(new FileUploadOptions(
        useTempFiles = true,
        tempFileDir = "/tmp/"
      )))

    // setup logging of the request - response cycles
    app.use { (request: Request, response: Response, next: NextFunction) =>
      val startTime = js.Date.now()
      next()
      response.onFinish { () =>
        val elapsedTime = js.Date.now() - startTime
        val query = if (request.query.nonEmpty) (request.query map { case (k, v) => s"$k=$v" } mkString ",").limitTo(120) else "..."
        logger.info("[node] application - %s %s (%s) ~> %d [%d ms]", request.method, request.path, query, response.statusCode, elapsedTime)
      }
    }

    // disable caching
    app.disable("etag")

    setupRoutes(app)
    app
  }

  private def setupRoutes(app: Application with WsRouting): Unit = {
    import routes.dao._

    // setup web socket routes
    logger.info("Setting up web socket...")
    app.ws("/websocket", (ws: WebSocket, request: Request) => ws.onMessage(WebSocketHandler.messageHandler(ws, request, _)))

    // setup all other routes
    logger.info("Setting up all other routes...")
    new AuthenticationRoutes(app)
    new ContestRoutes(app)
    new ExploreRoutes(app)
    new GlobalSearchRoutes(app)
    new NewsRoutes(app)
    new OrderRoutes(app)
    new PortfolioRoutes(app)
    new PostAttachmentRoutes(app)
    new PostRoutes(app)
    new QuoteRoutes(app)
    new RemoteEventRoutes(app)
    new ResearchRoutes(app)
    new RobotRoutes(app)
    new SocialRoutes(app)
    new TradingClockRoutes(app)
    new UserRoutes(app)

    // setup the autonomous trading robots
    logger.info("Setting up robots...")
    val robotProcessor = new RobotProcessor()
    setTimeout(() => robotProcessor.run(), 3.seconds)
    setInterval(() => robotProcessor.run(), 15.minute)

    // TODO remove this eventually ;-)
    uploadUserIcons()
  }

  private def uploadUserIcons()(implicit userDAO: UserDAO): Unit = {
    Seq(
      ("ldaniels", "./public/images/avatars/gears.jpg"),
      ("natech", "./public/images/avatars/dcu.png"),
      ("gunst4rhero", "./public/images/avatars/gunstar-heroes.jpg"),
      ("gadget", "./public/images/avatars/sickday.jpg"),
      ("daisy", "./public/images/avatars/daisy.jpg"),
      ("fugitive528", "./public/images/avatars/fugitive528.jpg")) foreach { case (name, path) =>
      UserRoutes.writeImage(name = name, path = path) onComplete {
        case Success(value) => println(s"$name ~> $path: count = $value")
        case Failure(e) =>
          println(s"Failed to set icon for user '$name':")
          e.printStackTrace()
      }
    }
  }

}