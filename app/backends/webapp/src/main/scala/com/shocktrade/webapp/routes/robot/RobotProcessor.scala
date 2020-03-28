package com.shocktrade.webapp.routes.robot

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.webapp.routes.contest.dao.{ContestDAO, OrderDAO}
import com.shocktrade.webapp.routes.research.dao.ResearchDAO
import com.shocktrade.webapp.routes.robot.RobotProcessor._
import com.shocktrade.webapp.routes.robot.dao.RobotDAO

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Robot Processor
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RobotProcessor()(implicit ec: ExecutionContext, contestDAO: ContestDAO, orderDAO: OrderDAO, researchDAO: ResearchDAO, robotDAO: RobotDAO) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val strategies = js.Dictionary[TradingStrategy](
    PENNY_STOCK -> new PennyStockTradingStrategy()
  )

  /**
   * Allows all robots an opportunity to invest
   */
  def run(): Unit = {
    val outcome = robotDAO.findRobots(isActive = true) map { robots =>
      for {
        robot <- robots.toList
        strategyName <- robot.strategy.toList
        strategy <- strategies.get(strategyName).toList
      } yield strategy.operate(robot)
    }

    outcome onComplete {
      case Success(_) =>
      case Failure(e) => logger.error("Robot failure", e)
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