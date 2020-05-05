package com.shocktrade.robots

import com.shocktrade.common.util.StringHelper._
import com.shocktrade.robots.dao.RobotDAO
import com.shocktrade.robots.routes.{NextFunction, RobotRoutes}
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.common.ProcessHelper._
import io.scalajs.nodejs.{process, _}
import io.scalajs.npm.bodyparser._
import io.scalajs.npm.express.{Application, Express, Request, Response}

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

object RobotsJsApp {
  private val logger = LoggerFactory.getLogger(getClass)

  @JSExport
  def main(args: Array[String]): Unit = {
    logger.info("Starting the ShockTrade Web Server...")

    // determine the port to listen on
    val startTime = js.Date.now()

    // setup the application
    val port = process.port getOrElse "9001"
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
  def configureApplication(): Application = {
    logger.info("Loading Express modules...")
    implicit val app: Application = Express()

    // setup the routes for serving static files
    logger.info("Setting up middleware...")
    app.use(Express.static("public"))
      .use("/bower_components", Express.static("bower_components"))
      .use(BodyParser.json())
      .use(BodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))

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
    app.disable("etag")

    // setup all routes
    setupRobots(app)
    app
  }

  /**
   * Setup the autonomous trading robots
   * @param app the given [[Application]]
   */
  private def setupRobots(app: Application): Unit = {
    logger.info("Setting up robots...")
    implicit val robotDAO: RobotDAO = RobotDAO()
    implicit val robotProcessor: RobotProcessor = new RobotProcessor()

    // setup intermittent processing
    robotDAO.findRobots map { robots =>
      logger.info(s"Setting up ${robots.size} robots...")
      for {
        robot <- robots
        robotName <- robot.username.toList
      } {
        setTimeout(() => robotProcessor.run(robotName), 1.milli)
        setInterval(() => robotProcessor.run(robotName), 5.minutes)
      }
    }

    // setup the robot routes
    new RobotRoutes(app)
    ()
  }

}
