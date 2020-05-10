package com.shocktrade.robots

import com.shocktrade.common.OrderConstants._
import com.shocktrade.common.forms.{NewOrderForm, ResearchOptions}
import com.shocktrade.common.models.contest.{OrderRef, PortfolioLike, Position}
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.common.models.user.UserProfile
import com.shocktrade.remote.proxies.{PortfolioProxy, ResearchProxy, UserProxy}
import com.shocktrade.robots.RobotProcessor._
import com.shocktrade.server.common.LoggerFactory
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Random, Success}

/**
 * Robot Processor
 * @param ec the implicit [[ExecutionContext]]
 */
class RobotProcessor(host: String = "localhost", port: Int = 9000)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val portfolioProxy = new PortfolioProxy(host, port)
  private val researchProxy = new ResearchProxy(host, port)
  private val userProxy = new UserProxy(host, port)
  private val random = new Random()

  def run(robotName: String): Unit = {
    try {
      start(robotName) onComplete {
        case Success(results) => //logger.info(JSON.stringify(results))
        case Failure(e) =>
          logger.error(e.getMessage)
          e.printStackTrace()
      }
    } catch {
      case e: Throwable =>
        logger.error(e.getMessage)
        e.printStackTrace()
    }
  }

  def start(robotName: String): Future[js.Array[RobotOrders]] = {
    for {
      orders <- produceOrders(robotName)
      _ <- saveOrders(orders)
    } yield orders
  }

  def findSecurities(robotName: String): Future[js.Array[ResearchQuote]] = {
    val options = robotName match {
      case "daisy" => new ResearchOptions(priceMax = 1.00, changeMax = 0.0, spreadMin = 25.0, volumeMin = 1e+6, maxResults = 25)
      case "gadget" => new ResearchOptions(priceMax = 5.00, maxResults = 25)
      case "joey" => new ResearchOptions(priceMin = 5.00, priceMax = 25.00, spreadMin = 25.0, maxResults = 25)
      case "teddy" => new ResearchOptions(priceMin = 1.00, priceMax = 5.00, spreadMin = 50.0, maxResults = 25)
      case "naughtymonkey" => new ResearchOptions(priceMin = 1.00, priceMax = 25.00, spreadMin = 25.0, maxResults = 25)
      case _ => new ResearchOptions(priceMax = random.nextDouble(), changeMax = 0.0, spreadMin = random.nextInt(75).toDouble, maxResults = 25)
    }
    researchProxy.research(options)
  }

  def produceOrders(robotName: String): Future[js.Array[RobotOrders]] = {
    for {
      robotUser <- userProxy.findUserByName(robotName)
      myPortfolios <- portfolioProxy.findPortfoliosByUser(robotUser.userID_!)
      portfolios = myPortfolios.filter(_.closedTime.flat.isEmpty)
      buyOrders <- Future.sequence(portfolios.toList.collect {
        case portfolio if portfolio.closedTime.flat.isEmpty & portfolio.funds.orZero > 100 =>
          import portfolio._
          for {
            allOrders <- portfolioProxy.findOrders(portfolio.contestID_!, robotUser.userID_!)
            stocks <- findSecurities(robotName)
          } yield {
            val outstandingOrders = allOrders.filter(o => o.closed.contains(0) && o.orderType.contains(BUY))
            val orderedSymbols = outstandingOrders.flatMap(_.symbol.toOption).distinct
            val filteredStocks = stocks.filterNot(_.symbol.exists(orderedSymbols.contains))
            val newOrders = produceBuyOrders(portfolio, filteredStocks, costTarget = funds.map(_ / filteredStocks.length).orZero)
            new RobotOrders(contestID, userID, portfolioID, orders = newOrders.toJSArray)
          }
      })
      sellOrders <- Future.sequence(portfolios.toList.collect {
        case portfolio if portfolio.closedTime.flat.isEmpty =>
          import portfolio._
          for {
            positions <- portfolioProxy.findPositions(portfolio.contestID_!, robotUser.userID_!)
            allOrders <- portfolioProxy.findOrders(portfolio.contestID_!, robotUser.userID_!)
          } yield {
            val outstandingOrders = allOrders.filter(o => o.closed.contains(0) && o.orderType.contains(SELL))
            val orderedSymbols = outstandingOrders.flatMap(_.symbol.toOption).distinct
            new RobotOrders(contestID, userID, portfolioID, orders = produceSellOrders(positions, orderedSymbols))
          }
      })
    } yield (buyOrders ::: sellOrders).toJSArray
  }

  private def produceBuyOrders(portfolio: PortfolioLike, stocks: Seq[ResearchQuote], costTarget: Double): List[NewOrderForm] = {
    case class Accumulator(orders: List[NewOrderForm] = Nil, budget: Double)
    val candidateOrders = for {
      stock <- stocks
      quantity <- stock.lastTrade.map(costTarget / _).map(_.toInt).toOption
    } yield stock.toBuyOrder(quantity)

    val results = candidateOrders.foldLeft(Accumulator(budget = portfolio.funds.orZero)) {
      case (acc@Accumulator(orders, budget), order) if order.totalCost.exists(tc => tc > 0.0 && tc <= budget) =>
        acc.copy(orders = order :: orders, budget = budget - order.totalCost.orZero)
      case (acc, _) => acc
    }
    results.orders
  }

  private def produceSellOrders(positions: js.Array[Position], orderedSymbols: js.Array[String]): js.Array[NewOrderForm] = {
    for {
      position <- positions.filterNot(p => p.symbol.exists(orderedSymbols.contains))
      lastTrade <- position.lastTrade.toList
      high <- position.high.toList
      low <- position.low.toList
      quantity <- position.quantity.toList if lastTrade >= high * 0.90
    } yield position.toSellOrder(quantity)
  }

  private def saveOrders(reports: js.Array[RobotOrders]): Future[js.Array[OrderRef]] = {
    Future.sequence(
      for {
        RobotOrders(contestID, userID, portfolioID, orders) <- reports.toSeq
        order <- orders
        outcome = portfolioProxy.createOrder(contestID = contestID, userID = userID, order)
      } yield outcome).map(_.toJSArray)
  }

}

/**
 * Robot Processor Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object RobotProcessor {

  class RobotOrders(val contestID: js.UndefOr[String],
                    val userID: js.UndefOr[String],
                    val portfolioID: js.UndefOr[String],
                    val orders: js.Array[NewOrderForm]) extends js.Object

  object RobotOrders {
    def unapply(r: RobotOrders): Option[(String, String, String, js.Array[NewOrderForm])] = {
      for {
        contestID <- r.contestID.toOption
        userID <- r.userID.toOption
        portfolioID <- r.portfolioID.toOption
      } yield (contestID, userID, portfolioID, r.orders)
    }
  }

  /**
   * User Profile Enriched
   * @param ref the host [[UserProfile]]
   */
  final implicit class UserProfileEnriched(val ref: UserProfile) extends AnyVal {
    def userID_! : String = ref.userID.getOrElse(throw js.JavaScriptException("User ID is required"))
  }

  /**
   * New Order Form Enriched
   * @param form the host [[NewOrderForm]]
   */
  final implicit class NewOrderFormEnriched(val form: NewOrderForm) extends AnyVal {
    def totalCost: js.UndefOr[Double] = for (price <- form.limitPrice; qty <- form.quantity) yield price * qty
  }

  /**
   * Research Quote Enriched
   * @param quote the host [[ResearchQuote]]
   */
  final implicit class ResearchQuoteEnriched(val quote: ResearchQuote) extends AnyVal {
    def toBuyOrder(quantity: Double): NewOrderForm = NewOrderForm(
      symbol = quote.symbol,
      exchange = quote.exchange,
      orderType = BUY,
      orderTerm = "3",
      priceType = Limit,
      quantity = quantity,
      limitPrice = quote.lastTrade)
  }

  /**
   * Position Enriched
   * @param position the host [[Position]]
   */
  final implicit class PositionEnriched(val position: Position) extends AnyVal {
    def toSellOrder(quantity: Double): NewOrderForm = NewOrderForm(
      symbol = position.symbol,
      exchange = position.exchange,
      orderType = SELL,
      orderTerm = "3",
      priceType = Limit,
      quantity = quantity,
      limitPrice = position.lastTrade)
  }

}