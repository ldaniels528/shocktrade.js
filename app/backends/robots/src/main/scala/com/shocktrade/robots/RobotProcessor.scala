package com.shocktrade.robots

import com.shocktrade.common.Ok
import com.shocktrade.common.forms.{NewOrderForm, ResearchOptions}
import com.shocktrade.common.models.contest.Portfolio
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.common.models.user.UserProfile
import com.shocktrade.remote.proxies.{ContestProxy, PortfolioProxy, ResearchProxy, UserProxy}
import com.shocktrade.robots.RobotProcessor.{RobotReport, _}
import com.shocktrade.server.common.LoggerFactory
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.reflectiveCalls
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Robot Processor
 * @param ec the implicit [[ExecutionContext]]
 */
class RobotProcessor(host: String = "localhost", port: Int = 9000)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val contestProxy = new ContestProxy(host, port)
  private val portfolioProxy = new PortfolioProxy(host, port)
  private val researchProxy = new ResearchProxy(host, port)
  private val userProxy = new UserProxy(host, port)

  def start(robotName: String): Future[js.Array[RobotReport]] = {
    for {
      orders <- createNewOrders(robotName)
      _ <- saveOrders(orders)
    } yield orders
  }

  def run(robotName: String): Unit = {
    start(robotName) onComplete {
      case Success(results) => //logger.info(JSON.stringify(results))
      case Failure(e) =>
        logger.error(e.getMessage)
        e.printStackTrace()
    }
  }

  def createNewOrders(robotName: String): Future[js.Array[RobotReport]] = {
    for {
      daisy <- userProxy.findUserByName(robotName)
      portfolios <- portfolioProxy.findPortfoliosByUser(daisy.userID_!)
      stocks <- findSecurities(robotName)
      orders = portfolios.map(p => new RobotReport(p.contestID, p.userID, p.portfolioID, produceOrders(p, stocks, costTarget = p.funds.map(_ / stocks.length).orZero)))
    } yield orders
  }

  def findSecurities(robotName: String): Future[js.Array[ResearchQuote]] = {
    val options = robotName match {
      case "daisy" => new ResearchOptions(priceMax = 1.00, changeMax = 0.0, spreadMin = 25.0, volumeMin = 1e+6, maxResults = 10)
      case "gadget" => new ResearchOptions(priceMax = 0.50, changeMax = 0.0, spreadMin = 30.0, volumeMin = 1e+6, maxResults = 10)
      case _ => new ResearchOptions(priceMax = 1.00, changeMax = 0.0, spreadMin = 10.0, volumeMin = 1e+6, maxResults = 10)
    }
    researchProxy.research(options)
  }

  private def produceOrders(portfolio: Portfolio, stocks: Seq[ResearchQuote], costTarget: Double): js.Array[NewOrderForm] = {
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
    results.orders.toJSArray
  }

  private def saveOrders(reports: js.Array[RobotReport]): Future[js.Array[Ok]] = {
    Future.sequence(
      for {
        RobotReport(contestID, userID, portfolioID, orders) <- reports.toSeq
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
  type OrderType = String
  val Buy: OrderType = "BUY"
  val Sell: OrderType = "SELL"

  type PriceType = String
  val Limit: PriceType = "LIMIT"
  val Market: PriceType = "MARKET"
  val MarketAtClose: PriceType = "MARKET_AT_CLOSE"

  class RobotReport(val contestID: js.UndefOr[String],
                    val userID: js.UndefOr[String],
                    val portfolioID: js.UndefOr[String],
                    val orders: js.Array[NewOrderForm]) extends js.Object

  object RobotReport {
    def unapply(r: RobotReport): Option[(String, String, String, js.Array[NewOrderForm])] = {
      for {
        contestID <- r.contestID.toOption
        userID <- r.userID.toOption
        portfolioID <- r.portfolioID.toOption
      } yield (contestID, userID, portfolioID, r.orders)
    }
  }

  final implicit class ContestSearchResultEnriched(val ref: Portfolio) extends AnyVal {
    def contestID_! : String = ref.contestID.getOrElse(throw js.JavaScriptException("Contest ID is required"))
    def userID_! : String = ref.userID.getOrElse(throw js.JavaScriptException("User ID is required"))
  }

  final implicit class UserProfileEnriched(val ref: UserProfile) extends AnyVal {
    def userID_! : String = ref.userID.getOrElse(throw js.JavaScriptException("User ID is required"))
  }

  final implicit class NewOrderFormMagic(val form: NewOrderForm) extends AnyVal {
    def totalCost: js.UndefOr[Double] = for (price <- form.limitPrice; qty <- form.quantity) yield price * qty
  }

  final implicit class ResearchQuoteMagic(val quote: ResearchQuote) extends AnyVal {

    def toBuyOrder(quantity: Double) = new NewOrderForm(
      symbol = quote.symbol,
      exchange = quote.exchange,
      orderType = Buy,
      orderTerm = "3",
      priceType = Limit,
      quantity = quantity,
      limitPrice = quote.lastTrade)

    def toSellOrder(quantity: Double) = new NewOrderForm(
      symbol = quote.symbol,
      exchange = quote.exchange,
      orderType = Sell,
      orderTerm = "3",
      priceType = Limit,
      quantity = quantity,
      limitPrice = quote.lastTrade)

  }

}