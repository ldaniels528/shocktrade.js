package com.shocktrade.robots

import com.shocktrade.common.OrderConstants._
import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.common.models.contest._
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.remote.proxies.{ContestProxy, PortfolioProxy, ResearchProxy, UserProxy}
import com.shocktrade.robots.RobotProcessor._
import com.shocktrade.robots.dao.RobotPortfolioData
import com.shocktrade.server.common.LoggerFactory
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Robot Processor
 * @param ec the implicit [[ExecutionContext]]
 */
class RobotProcessor(host: String = "localhost", port: Int = 9000)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private implicit val contestProxy: ContestProxy = new ContestProxy(host, port)
  private implicit val portfolioProxy: PortfolioProxy = new PortfolioProxy(host, port)
  private implicit val researchProxy: ResearchProxy = new ResearchProxy(host, port)
  private implicit val userProxy: UserProxy = new UserProxy(host, port)
  private val states = js.Dictionary[RobotState]()
  private val isDebug = true

  /**
   * Starts a robot by name
   * @param robotName the given robot name
   * @return the resulting collection of [[RobotActivity robot activities]]
   */
  def start(robotName: String, portfolio: RobotPortfolioData): Future[js.Array[RobotActivity]] = {
    val state = states.getOrElseUpdate(robotName, RobotState(robotName, portfolio.contestID.orNull, portfolio.userID.orNull, portfolio))
    implicit val personality: Personality = Personality(state)
    for {
      rankings <- contestProxy.findContestRankings(state.contestID)
      _ <- checkForOvertakes(state, rankings)
      orders <- produceOrders(state)
      _ <- saveOrders(orders)
    } yield orders.map(_.asInstanceOf[RobotActivity])
  }

  private def checkForOvertakes(state: RobotState, rankings: js.Array[ContestRanking])(implicit personality: Personality): Future[Unit] = {
    val myRanking = rankings.find(_.username.contains(state.robotName))
    if (state.prevRankNum_?.isEmpty) state.prevRankNum_? = myRanking.flatMap(_.rankNum.toOption)
    (for {
      prevRankNum <- state.prevRankNum_?
      newRankNum <- myRanking.flatMap(_.rankNum.toOption)
      promise <- if (prevRankNum <= newRankNum) None else {
        personality.onOvertake(oldRank = prevRankNum, newRank = newRankNum,
          me = myRanking.orUndefined, him = rankings.find(_.rankNum.contains(prevRankNum)).orUndefined)
          .map(sendChatMessage(state, _))
          .toOption
      }
      _ = state.prevRankNum_? = Option(newRankNum)
    } yield promise) getOrElse Future.successful({})
  }

  private def findSecurities()(implicit personality: Personality): Future[js.Array[ResearchQuote]] = {
    researchProxy.research(personality.onBuying())
  }

  private def produceOrders(state: RobotState)(implicit personality: Personality): Future[js.Array[RobotOrders]] = {
    for {
      robotUser <- userProxy.findUserByName(state.robotName)
      myPortfolios <- portfolioProxy.findPortfoliosByUser(robotUser.userID_!)
      portfolios = myPortfolios.filter(_.closedTime.flat.isEmpty)
      buyOrders <- Future.sequence(portfolios.toList.collect {
        case portfolio if portfolio.closedTime.flat.isEmpty & portfolio.funds.orZero > 100 =>
          import portfolio._
          for {
            allOrders <- portfolioProxy.findOrders(portfolio.contestID_!, robotUser.userID_!).map(_.map(_.asInstanceOf[MyOrder]))
            stocks <- findSecurities()
          } yield {
            val outstandingOrders = allOrders.filter(o => o.closed.contains(0) && o.orderType.contains(BUY))
            val outstandingOrderTotal = outstandingOrders.flatMap(o => (for {price <- o.price ?? o.lastTrade; qty <- o.quantity} yield price * qty).toOption).sum
            val orderedSymbols = outstandingOrders.flatMap(_.symbol.toOption).distinct
            val filteredStocks = stocks.filterNot(_.symbol.exists(orderedSymbols.contains))
            val eligibleStocks = if (funds.exists(_ >= outstandingOrderTotal)) filteredStocks else js.Array()
            val newOrders = produceBuyOrders(portfolio, eligibleStocks, costTarget = funds.map(_ / eligibleStocks.length).orZero)
            if(isDebug) {
              import state.robotName
              logger.info(s"$robotName: ${stocks.size} stocks identified")
              logger.info(f"$robotName: funds: ${funds.orZero}%.2f | outstanding orders: $outstandingOrderTotal%.2f")
              logger.info(s"$robotName: ${orderedSymbols.size} ordered symbols [${orderedSymbols.mkString(",")}]")
              logger.info(s"$robotName: ${filteredStocks.size} filtered stocks")
              logger.info(s"$robotName: ${eligibleStocks.size} eligible stocks")
              logger.info(s"$robotName: ${newOrders.size} new orders")
              logger.info("*" * 60)
            }
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
      low <- stock.low.toList
      limitPrice = low * 1.1
      quantity = (costTarget / limitPrice).toInt if quantity >= 25
    } yield stock.toBuyOrder(quantity, limitPrice)

    val results = candidateOrders.foldLeft(Accumulator(budget = portfolio.funds.orZero)) {
      case (acc@Accumulator(orders, budget), order) if order.totalCost.exists(tc => tc > 0.0 && tc <= budget) =>
        acc.copy(orders = order :: orders, budget = budget - order.totalCost.orZero)
      case (acc, _) => acc
    }
    results.orders
  }

  private def produceSellOrders(positions: js.Array[Position], orderedSymbols: js.Array[String]): js.Array[NewOrderForm] = {
    for {
      position <- positions.filterNot(_.symbol.exists(orderedSymbols.contains))
      high <- position.high.toList
      quantity <- position.quantity.toList
    } yield position.toSellOrder(quantity, limitPrice = high * 0.90)
  }

  private def saveOrders(reports: js.Array[RobotOrders]): Future[js.Array[OrderRef]] = {
    Future.sequence(
      for {
        RobotOrders(contestID, userID, portfolioID, orders) <- reports.toSeq
        order <- orders
        outcome = portfolioProxy.createOrder(contestID = contestID, userID = userID, order)
      } yield outcome).map(_.toJSArray)
  }

  private def sendChatMessage(state: RobotState, message: String): Future[Unit] = {
    contestProxy.sendChatMessage(state.contestID, new ChatMessage(
      userID = state.userID,
      username = state.robotName,
      message = message
    )).map(_ => ())
  }

}

/**
 * Robot Processor Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object RobotProcessor {

  /**
   * Custom Order Type
   */
  sealed trait MyOrder extends OrderLike {
    def lastTrade: js.UndefOr[Double]
  }

  /**
   * Base trait for Robot Activities
   */
  sealed trait RobotActivity extends js.Object

  /**
   * Robot Orders
   * @param contestID   the given contest ID
   * @param userID      the given user ID
   * @param portfolioID the given portfolio ID
   * @param orders      the given [[NewOrderForm orders]]
   */
  class RobotOrders(val contestID: js.UndefOr[String],
                    val userID: js.UndefOr[String],
                    val portfolioID: js.UndefOr[String],
                    val orders: js.Array[NewOrderForm]) extends RobotActivity

  /**
   * Robot Orders Companion
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
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
    def toBuyOrder(quantity: Double, limitPrice: Double): NewOrderForm = NewOrderForm(
      symbol = quote.symbol,
      exchange = quote.exchange,
      orderType = BUY,
      orderTerm = "1",
      priceType = Limit,
      quantity = quantity,
      limitPrice = limitPrice)
  }

  /**
   * Position Enriched
   * @param position the host [[Position]]
   */
  final implicit class PositionEnriched(val position: Position) extends AnyVal {
    def toSellOrder(quantity: Double, limitPrice: Double): NewOrderForm = NewOrderForm(
      symbol = position.symbol,
      exchange = position.exchange,
      orderType = SELL,
      orderTerm = "1",
      priceType = Limit,
      quantity = quantity,
      limitPrice = limitPrice)
  }

}