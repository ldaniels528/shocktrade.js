package com.shocktrade.webapp.routes.robot

import com.shocktrade.common.models.contest.ContestRef
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.webapp.routes.dao._
import com.shocktrade.webapp.routes.robot.RobotProcessor._
import com.shocktrade.webapp.routes.robot.dao.{RobotDAO, RobotData}
import io.scalajs.JSON

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Robot Processor
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RobotProcessor()(implicit ec: ExecutionContext, robotDAO: RobotDAO) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val strategies = js.Dictionary[TradingStrategy](
    PENNY_STOCK -> new PennyStockTradingStrategy()
  )

  /**
   * Allows all robots an opportunity to invest
   */
  def run(): Unit = {
    start() onComplete {
      case Success(responses) =>
        responses foreach { response =>
          if (response.counts > 0) logger.info(s"response: $response")
        }
      case Failure(e) =>
        logger.error(s"Robot failure '${e.getMessage}'")
        if (!e.getMessage.contains("predicate is not satisfied")) {
          e.printStackTrace()
        }
    }
  }

  /**
   * Allows all robots an opportunity to invest
   */
  def start(): Future[List[RobotActivity]] = {
    robotDAO.findRobots(isActive = true).flatMap(execute).map { results =>
      logger.info("Done.")
      results
    }
  }

  private def execute(robots: js.Array[RobotData]): Future[List[RobotActivity]] = {
    for {
      activitiesA <- findContestsToJoin(robots)
      activitiesB <- managePortfolios(robots)
    } yield activitiesA ::: activitiesB
  }

  private def findContestsToJoin(robots: js.Array[RobotData]): Future[List[RobotActivity]] = {
    val robotRefs = robots.groupBy(_.userID).flatMap { case (_, list) => list.headOption }.map(RobotRef.apply).toList
    val outcome = Future.sequence(robotRefs map { case robotRef@RobotRef(robotUserID, robotName, _, _) =>
      for {
        contestRefs <- robotDAO.findContestsToJoin(robotName, limit = 1)
        counts <- Future.sequence(contestRefs.toSeq map {
          case ContestRef(contestID, _) =>
            contestDAO.join(contestID, robotUserID) recover {
              case e: Throwable =>
                logger.error(s"Failed to join contest '$contestID': ${e.getMessage}")
                0
            }
          case unknown =>
            logger.error(s"Failed join match: ${JSON.stringify(unknown)}")
            Future.successful(0)
        }).map(_.sum)
      } yield JoinedContestActivity(robotRef, contestRefs, counts)
    })

    outcome recover {
      case e: Throwable =>
        logger.error(s"Complete failure joining contests: ${e.getMessage}")
        Nil
    }
  }

  private def managePortfolios(robots: js.Array[RobotData]): Future[List[RobotActivity]] = {
    val outcome = Future.sequence {
      for {
        robot <- robots.toList
        strategyName <- robot.strategy.toList
        strategy <- strategies.get(strategyName).toList
      } yield strategy.operate(robot)
    } map (_.flatten)

    outcome recover {
      case e: Throwable =>
        logger.error(s"Complete failure to managing portfolios: ${e.getMessage}")
        Nil
    }
  }

}

/**
 * Robot Processor
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object RobotProcessor {
  val PENNY_STOCK = "penny-stock"

}