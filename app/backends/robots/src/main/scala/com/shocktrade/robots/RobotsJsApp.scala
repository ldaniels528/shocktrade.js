package com.shocktrade.robots

import com.shocktrade.common.util.StringHelper._
import com.shocktrade.robots.RobotProcessor.RobotActivity
import com.shocktrade.robots.dao.{RobotDAO, RobotPortfolioData}
import com.shocktrade.robots.routes.{NextFunction, RobotRoutes}
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.common.ProcessHelper._
import io.scalajs.nodejs._
import io.scalajs.npm.bodyparser._
import io.scalajs.npm.express.{Application, Express, Request, Response}

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
 * Robot Trading Application
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object RobotsJsApp {
  private val logger = LoggerFactory.getLogger(getClass)

  @JSExport
  def main(args: Array[String]): Unit = {
    logger.info("Starting the Robot Trading Server...")

    // determine the port to listen on
    val startTime = System.currentTimeMillis()

    // setup the application
    val port = process.port getOrElse "9001"
    val app = configureApplication()
    app.listen(port, () => logger.info(s"Server now listening on port $port [${System.currentTimeMillis() - startTime} msec]"))

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
    implicit val robotDAO: RobotDAO = RobotDAO()
    implicit val robotProcessor: RobotProcessor = new RobotProcessor()

    def run(): Unit = {
      val startTime = System.currentTimeMillis()
      val outcome = for {
        robots <- robotDAO.findRobotPortfolios
        orders <- invokeRobots(robots)
      } yield (robots, orders)

      outcome onComplete {
        case Success((robots, orders)) =>
          val elapsedTime = System.currentTimeMillis() - startTime
          if (orders.nonEmpty) logger.info(s"${robots.size} robots produced ${orders.size} orders created in $elapsedTime msec")
        case Failure(e) =>
          logger.error(e.getMessage)
      }
    }

    // start and run the robots every 5 minutes
    setTimeout(() => run(), 0.millis)
    setInterval(() => run(), 1.minutes)

    // setup the robot routes
    new RobotRoutes(app)
    ()
  }

  def invokeRobots(robots: Seq[RobotPortfolioData])(implicit robotProcessor: RobotProcessor): Future[js.Array[RobotActivity]] = {
    val promise = Promise[js.Array[RobotActivity]]()
    var results: List[RobotActivity] = Nil

    def recurse(myRobots: List[RobotPortfolioData]): Unit = {
      myRobots match {
        case _robot :: _robots =>
          try {
            val name = _robot.username.orNull
            robotProcessor.start(name, _robot) onComplete {
              case Success(newOrders) =>
                results = newOrders.toList ::: results
                recurse(_robots)
              case Failure(e) =>
                logger.error(s"$name: ${e.getMessage}")
                recurse(_robots)
            }
          } catch {
            case e: Exception =>
              logger.error(s"${_robot}: ${e.getMessage}")
          }
        case Nil => promise.success(results.reverse.toJSArray)
      }
    }

    setImmediate(() => recurse(robots.toList))
    promise.future
  }

}
