package com.shocktrade.webapp.routes.robot

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.webapp.routes.contest.OrderTypes.OrderType
import com.shocktrade.webapp.routes.contest.PriceTypes.PriceType
import com.shocktrade.webapp.routes.contest.dao.OrderData
import com.shocktrade.webapp.routes.contest.{OrderTypes, PriceTypes}
import com.shocktrade.webapp.routes.robot.TradingStrategy._
import com.shocktrade.webapp.routes.robot.dao.{RobotDAO, RobotData}
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Penny Stock Trading Strategy
 * @param ec the implicit [[ExecutionContext]]
 */
class PennyStockTradingStrategy()(implicit ec: ExecutionContext, robotDAO: RobotDAO) extends TradingStrategy {

  override def buySecurities()(implicit robot: RobotData): Future[OrderBuyActivity] = {
    for {
      robotRef <- findRobotRef
      stocks <- findStocksToBuy(new ResearchOptions(priceMax = 1.0, changeMax = 0.0, spreadMin = 50.0, volumeMin = 1e+6, maxResults = 50))
      orders = makeOrders(orderType = OrderTypes.Buy, stocks = stocks, priceType = PriceTypes.Limit, costTarget = 2500.0)
      counts <- saveOrders(orders)
    } yield OrderBuyActivity(robotRef, stocks, orders, counts)
  }

  override def sellSecurities()(implicit robot: RobotData): Future[OrderSellActivity] = {
    for {
      robotRef <- findRobotRef
      stocks <- findStocksToSell(new ResearchOptions(priceMax = 1.0, changeMax = 0.0, spreadMin = 50.0, volumeMin = 1e+6, maxResults = 50))
      orders = makeOrders(orderType = OrderTypes.Sell, stocks = stocks, priceType = PriceTypes.Limit, costTarget = 2500.0)
      counts <- saveOrders(orders)
    } yield OrderSellActivity(robotRef, stocks, orders, counts)
  }

  private def makeOrders(stocks: Seq[ResearchQuote],
                         orderType: OrderType,
                         priceType: PriceType,
                         costTarget: Double)(implicit robot: RobotData): js.Array[OrderData] = {
    case class Accumulator(orders: List[OrderData] = Nil, budget: Double)
    val candidateOrders = for {
      stock <- stocks
      quantity <- stock.lastTrade.map(costTarget / _).toOption
      order = stock.toOrder(orderType = orderType, quantity = quantity, priceType = priceType)
    } yield order

    val results = candidateOrders.foldLeft[Accumulator](Accumulator(budget = robot.funds.orZero)) {
      case (acc@Accumulator(orders, budget), order) if order.totalCost.exists(tc => tc > 0.0 && tc <= budget) =>
        acc.copy(orders = order :: orders, budget = budget - order.totalCost.orZero)
      case (acc, _) => acc
    }
    results.orders.toJSArray
  }

}
