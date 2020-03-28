package com.shocktrade.webapp.routes.robot

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.webapp.routes.contest.PriceTypes
import com.shocktrade.webapp.routes.contest.dao.{ContestDAO, OrderDAO, OrderData}
import com.shocktrade.webapp.routes.research.dao.ResearchDAO
import com.shocktrade.webapp.routes.robot.dao.{RobotDAO, RobotData}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

/**
 * Penny Stock Trading Strategy
 * @param ec the implicit [[ExecutionContext]]
 */
class PennyStockTradingStrategy()(implicit ec: ExecutionContext, contestDAO: ContestDAO, orderDAO: OrderDAO, researchDAO: ResearchDAO, robotDAO: RobotDAO)
  extends TradingStrategy {
  private val logger = LoggerFactory.getLogger(getClass)
  private val random = new Random()

  override def operate(robot: RobotData): Unit = {
    implicit val _robot: RobotData = robot
    findContestsToJoin()
    buySecurities()
    sellSecurities()
  }

  private def findContestsToJoin()(implicit contestDAO: ContestDAO, robot: RobotData) = {

  }

  ///////////////////////////////////////////////////////
  //    Buying Securities
  ///////////////////////////////////////////////////////

  private def buySecurities()(implicit robot: RobotData, contestDAO: ContestDAO, orderDAO: OrderDAO, robotDAO: RobotDAO, researchDAO: ResearchDAO): Future[(Seq[ResearchQuote], List[OrderData], Int)] = {
    val robotName = robot.username.orNull
    val startTime = System.currentTimeMillis()
    val outcome = for {
      _ <- setRobotActivity("Looking for stocks to buy")
      stocks <- findStocksToBuy(new ResearchOptions(priceMax = 1.0, changeMax = 0.0, spreadMin = 50.0, volumeMin = 1e+6, maxResults = 50))
      orders = makeBuyOrders(stocks, priceType = PriceTypes.Limit, costTarget = 2500.0)
      _ <- makeOrdersMessage(orders).map(sendChat).getOrElse(Future.successful(0))
      counts <- saveOrders(orders)
    } yield (stocks, orders, counts)

    outcome onComplete {
      case Success((stocks, orders, counts)) =>
        if (orders.nonEmpty) logger.info(s"$robotName | ${orders.size} orders created | ${stocks.size} quotes matched | $counts records updated [${System.currentTimeMillis() - startTime} msec]")
      case Failure(e) => logger.error(s"$robotName | Failed to buy securities", e)
    }
    outcome
  }

  private def makeOrdersMessage(orders: Seq[OrderData]): Option[String] = {
    if (orders.isEmpty) None else {
     val message = random.nextInt(3) match {
        case 0 => s"I just placed ${orders.size} orders."
        case 1 => "Just bought some stocks..."
        case _ => "You're so not ready for what's coming!"
      }
      Some(message)
    }
  }

  ///////////////////////////////////////////////////////
  //    Selling Securities
  ///////////////////////////////////////////////////////

  private def sellSecurities()(implicit robot: RobotData): Unit = {
    // TODO figure this out
  }

}
