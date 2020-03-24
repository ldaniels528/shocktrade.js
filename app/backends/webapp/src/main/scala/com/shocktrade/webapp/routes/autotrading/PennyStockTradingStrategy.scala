package com.shocktrade.webapp.routes.autotrading

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.contest.OrderLike._
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.webapp.routes.autotrading.PennyStockTradingStrategy._
import com.shocktrade.webapp.routes.autotrading.dao.{RobotDAO, RobotData}
import com.shocktrade.webapp.routes.contest.dao.{OrderDAO, OrderData}
import com.shocktrade.webapp.routes.contest.{OrderTypes, PriceTypes}
import com.shocktrade.webapp.routes.research.dao.ResearchDAO
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Penny Stock Trading Strategy
 * @param ec the implicit [[ExecutionContext]]
 */
class PennyStockTradingStrategy()(implicit ec: ExecutionContext) extends TradingStrategy {
  private val logger = LoggerFactory.getLogger(getClass)
  private val orderDAO = OrderDAO()
  private val researchDAO = ResearchDAO()
  private val robotDAO = RobotDAO()

  override def operate(robot: RobotData): Unit = {
    buySecurities()(robot)
    sellSecurities()(robot)
  }

  ///////////////////////////////////////////////////////
  //    Buying Securities
  ///////////////////////////////////////////////////////

  private def buySecurities()(implicit robot: RobotData): Future[(Seq[ResearchQuote], List[OrderData], Int)] = {
    val startTime = System.currentTimeMillis()
    val outcome = for {
      stocks <- findStocksToBuy
      orders = createBuyOrders(stocks)
      counts <- persistOrders(orders)
    } yield (stocks, orders, counts)

    val robotName = robot.username.orNull
    outcome onComplete {
      case Success((stocks, orders, counts)) =>
        if (orders.nonEmpty) logger.info(s"$robotName | ${orders.size} orders created | ${stocks.size} quotes matched | $counts records updated [${System.currentTimeMillis() - startTime} msec]")
      case Failure(e) => logger.error(s"$robotName | Failed to buy securities", e)
    }
    outcome
  }

  private def createBuyOrders(stocks: Seq[ResearchQuote])(implicit robot: RobotData): List[OrderData] = {
    val results = stocks.map(_.toOrder(quantity = 1000)).foldLeft[(List[OrderData], Double)](Nil -> robot.funds.orZero) {
      case ((orders, budget), order) if order.totalCost.exists(_ <= budget) => (order :: orders, budget - order.totalCost.orZero)
      case ((orders, budget), _) => (orders, budget)
    }
    val orders = results._1
    orders
  }

  private def findStocksToBuy(implicit robot: RobotData): Future[Seq[ResearchQuote]] = {
    val options = new ResearchOptions(priceMax = 1.0, changeMax = 0.0, spreadMin = 50.0, volumeMin = 1e+6, maxResults = 50)
    for {
      stocks <- researchDAO.research(options)
      pendingOrderedSymbols <- robotDAO.findPendingOrderSymbols(robot.username.orNull, robot.portfolioID.orNull)
    } yield {
      stocks
        .filterNot(s => pendingOrderedSymbols.contains(s.symbol.orNull))
        .sortBy(r => (-r.spread.orZero, r.lastTrade.orZero, -r.volume.orZero))
    }
  }

  private def persistOrders(orders: Seq[OrderData])(implicit robot: RobotData): Future[Int] = {
    Future.sequence(orders.map(o => orderDAO.createOrder(robot.portfolioID.orNull, o))).map(_.sum)
  }

  ///////////////////////////////////////////////////////
  //    Selling Securities
  ///////////////////////////////////////////////////////

  private def sellSecurities()(implicit robot: RobotData): Unit = {
    // TODO figure this out
  }

}

/**
 * Penny Stock Trading Strategy
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PennyStockTradingStrategy {

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