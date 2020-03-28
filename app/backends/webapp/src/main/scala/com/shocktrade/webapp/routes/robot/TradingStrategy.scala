package com.shocktrade.webapp.routes.robot

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.webapp.routes.contest.PriceTypes.PriceType
import com.shocktrade.webapp.routes.contest.dao.{ContestDAO, OrderDAO, OrderData}
import com.shocktrade.webapp.routes.contest.{OrderTypes, PriceTypes}
import com.shocktrade.webapp.routes.research.dao.ResearchDAO
import com.shocktrade.webapp.routes.robot.TradingStrategy._
import com.shocktrade.webapp.routes.robot.dao.{RobotDAO, RobotData}
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.Future
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

  protected def makeBuyOrders(stocks: Seq[ResearchQuote], priceType: PriceType, costTarget: Double)
                             (implicit robot: RobotData): List[OrderData] = {
    case class Accumulator(orders: List[OrderData] = Nil, budget: Double)
    val candidateOrders = for {
      stock <- stocks
      quantity <- stock.lastTrade.map(costTarget / _).toOption
      order = stock.toOrder(quantity = quantity, priceType = priceType)
    } yield order

    val results = candidateOrders.foldLeft[Accumulator](Accumulator(budget = robot.funds.orZero)) {
      case (acc@Accumulator(orders, budget), order) if order.totalCost.exists(tc => tc > 0.0 && tc <= budget) =>
        acc.copy(orders = order :: orders, budget = budget - order.totalCost.orZero)
      case (acc, _) => acc
    }
    results.orders
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

  /**
   * Research Quote Enrichment
   * @param quote the given [[ResearchQuote]]
   */
  final implicit class ResearchQuoteEnrichment(val quote: ResearchQuote) extends AnyVal {

    def toOrder(quantity: Double, priceType: PriceType, price: js.UndefOr[Double] = js.undefined): OrderData = {
      new OrderData(
        symbol = quote.symbol,
        exchange = quote.symbol,
        orderType = OrderTypes.Buy,
        priceType = priceType,
        price = price ?? quote.lastTrade,
        quantity = quantity,
        creationTime = new js.Date(),
        expirationTime = js.undefined,
        processedTime = js.undefined,
        statusMessage = js.undefined,
        closed = false)
    }
  }
}