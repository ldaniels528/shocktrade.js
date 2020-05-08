package com.shocktrade.webapp

import com.shocktrade.common.util.StringHelper._
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.common.ProcessHelper._
import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.routes.account._
import com.shocktrade.webapp.routes.contest.{VirtualMachineRoutes, _}
import com.shocktrade.webapp.routes.discover._
import com.shocktrade.webapp.routes.research.ResearchRoutes
import com.shocktrade.webapp.routes.social.{PostAttachmentRoutes, PostRoutes, SocialRoutes}
import com.shocktrade.webapp.vm.VirtualMachine
import com.shocktrade.webapp.vm.dao.VirtualMachineDAO
import com.shocktrade.webapp.vm.proccesses.cqm.ContestQualificationModule
import io.scalajs.nodejs._
import io.scalajs.npm.bodyparser._
import io.scalajs.npm.express.fileupload.{ExpressFileUpload, FileUploadOptions}
import io.scalajs.npm.express.{Application, Express, Request, Response}
import io.scalajs.npm.expressws.{ExpressWS, WebSocket, WsInstance, WsRouterExtensions, WsRouting}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
 * ShockTrade Web Server Application
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
    ()
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
    app.use({ (request: Request, response: Response, next: NextFunction) =>
      val startTime = js.Date.now()
      next()
      response.onFinish { () =>
        val elapsedTime = js.Date.now() - startTime
        val query = if (request.query.nonEmpty) (request.query map { case (k, v) => s"$k=$v" } mkString ",").limitTo(120) else "..."
        logger.info("[node] application - %s %s (%s) ~> %d [%d ms]", request.method, request.path, query, response.statusCode, elapsedTime)
      }
    }: js.Function)

    // disable caching
    //app.disable("etag")

    // setup all routes
    setupWebSocket(app)
    setupRoutes(app)
    app
  }

  /**
   * Setup all application routes
   * @param app the given [[Application]]
   */
  private def setupRoutes(app: Application with WsRouting): Unit = {
    import routes.dao._

    logger.info("Setting up Virtual Machine routes...")
    implicit val vmDAO: VirtualMachineDAO = VirtualMachineDAO()
    implicit val cqm: ContestQualificationModule = new ContestQualificationModule()
    implicit val vm: VirtualMachine = new VirtualMachine()

    // setup all other routes
    logger.info("Setting up all other routes...")
    new ContestRoutes(app)
    new ExploreRoutes(app)
    new GlobalSearchRoutes(app)
    new PortfolioRoutes(app)
    new PostAttachmentRoutes(app)
    new PostRoutes(app)
    new QuoteRoutes(app)
    new RemoteEventRoutes(app)
    new ResearchRoutes(app)
    new RSSFeedRoutes(app)
    new SocialRoutes(app)
    new TradingClockRoutes(app)
    new UserRoutes(app)
    new VirtualMachineRoutes(app)
    ()
  }

  /**
   * Setup the web socket route
   * @param app the given [[Application]]
   */
  private def setupWebSocket(app: Application with WsRouting): Unit ={
    // setup web socket routes
    logger.info("Setting up web socket...")
    app.ws("/websocket", (ws: WebSocket, request: Request) => ws.onMessage(WebSocketHandler.messageHandler(ws, request, _)))
  }

}