package com.shocktrade.webapp.routes.robot

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.webapp.routes.robot.dao.RobotDAO

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Robot Processor
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RobotProcessor()(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val robotDAO = RobotDAO()

  /**
   * Allows all robots an opportunity to invest
   */
  def run(): Unit = {
    val outcome = for {
      robots <- robotDAO.findRobots(isActive = true)
    } yield {
      robots map { robot => TradingStrategy.withName(robot.strategy.orNull).foreach(_.operate(robot)) }
    }

    outcome onComplete {
      case Success(_) =>
      case Failure(e) => logger.error("Robot failure", e)
    }
  }

}