package com.shocktrade.webapp.routes.robot

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.webapp.routes.contest.dao.{ContestDAO, OrderDAO, OrderData}
import com.shocktrade.webapp.routes.contest.{OrderTypes, PriceTypes}
import com.shocktrade.webapp.routes.research.dao.ResearchDAO
import com.shocktrade.webapp.routes.robot.TradingStrategy._
import com.shocktrade.webapp.routes.robot.dao.{RobotDAO, RobotData}
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

/**
 * Represents a Trading Strategy
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait TradingStrategy {

  /**
   * Allows the robot to act autonomously
   * @param robot the given [[RobotData]]
   */
  def operate(robot: RobotData): Unit

  protected def findStocksToBuy(options: ResearchOptions)(implicit robot: RobotData, researchDAO: ResearchDAO, robotDAO: RobotDAO): Future[Seq[ResearchQuote]] = {
    for {
      stocks <- researchDAO.research(options)
      pendingOrderedSymbols <- robotDAO.findPendingOrderSymbols(robot.username.orNull, robot.portfolioID.orNull)
    } yield {
      stocks
        .filterNot(s => pendingOrderedSymbols.contains(s.symbol.orNull))
        .sortBy(r => (-r.spread.orZero, r.lastTrade.orZero, -r.volume.orZero))
    }
  }

  protected def makeBuyOrders(stocks: Seq[ResearchQuote])(implicit robot: RobotData): List[OrderData] = {
    val results = stocks.map(_.toOrder(quantity = 1000)).foldLeft[(List[OrderData], Double)](Nil -> robot.funds.orZero) {
      case ((orders, budget), order) if order.totalCost.exists(_ <= budget) => (order :: orders, budget - order.totalCost.orZero)
      case ((orders, budget), _) => (orders, budget)
    }
    val orders = results._1
    orders
  }

  protected def saveOrders(orders: Seq[OrderData])(implicit robot: RobotData, orderDAO: OrderDAO): Future[Int] = {
    Future.sequence(orders.map(o => orderDAO.createOrder(robot.portfolioID.orNull, o))).map(_.sum)
  }

  protected def sendChat(message: String)(implicit robot: RobotData, contestDAO: ContestDAO, robotDAO: RobotDAO): Future[Int] = {
    val contestID = robot.contestID.orNull
    val userID = robot.userID.orNull
    for {
      _ <- setRobotActivity(s"Sent a message to contest $contestID")
      count <- contestDAO.addChatMessage(contestID, userID, message)
    } yield count
  }

  protected def setRobotActivity(message: String)(implicit robot: RobotData, robotDAO: RobotDAO): Future[Int] = {
    val username = robot.username.orNull
    robotDAO.setRobotActivity(username, message)
  }

}

/**
 * Trading Strategy
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object TradingStrategy {
  val PENNY_STOCK = "penny-stock"

  def withName(strategyName: String)(implicit ec: ExecutionContext): Option[TradingStrategy] = strategyName match {
    case "penny-stock" => Some(new PennyStockTradingStrategy())
    case _ => None
  }

  /**
   * Research Quote Enrichment
   * @param quote the given [[ResearchQuote]]
   */
  final implicit class ResearchQuoteEnrichment(val quote: ResearchQuote) extends AnyVal {

    def toOrder(quantity: Double) = new OrderData(
      symbol = quote.symbol,
      exchange = quote.symbol,
      orderType = OrderTypes.Buy,
      priceType = PriceTypes.Market,
      price = quote.lastTrade,
      quantity = quantity,
      creationTime = new js.Date(),
      expirationTime = js.undefined,
      processedTime = js.undefined,
      statusMessage = js.undefined,
      closed = false
    )
  }
}