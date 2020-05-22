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
import scala.language.postfixOps
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
  private val states = js.Dictionary[RobotContext]()
  private val isDebug = true

  /**
   * Allows the robot to conduct its trading activities
   * @param robotName the given robot name
   * @param portfolio the given [[RobotPortfolioData]]
   * @return the resulting collection of [[NewOrderForm orders]]
   */
  def start(robotName: String, portfolio: RobotPortfolioData): Future[js.Array[NewOrderForm]] = {
    // get or create the robot's context
    implicit val state: RobotContext = states.getOrElseUpdate(robotName, RobotContext(
      robotName = robotName,
      contestID = portfolio.contestID_!,
      portfolioID = portfolio.portfolioID_!,
      userID = portfolio.userID_!,
      previousRankings = js.undefined
    ))

    // get the robot's personality
    implicit val personality: Personality = Personality(state)

    for {
      orders <- produceOrders()
      _ <- saveOrders(orders)
      rankings <- contestProxy.findContestRankings(portfolio.contestID_!).map(_.sortBy(_.rankNum.getOrElse(Int.MaxValue)))
      _ <- handleRankChanges(rankings)
      _ = state.previousRankings = rankings
    } yield orders
  }

  private def findSecurities(implicit personality: Personality, state: RobotContext): Future[js.Array[ResearchQuote]] = {
    personality.onFindStocksToBuy.map(researchProxy.research) getOrElse Future.successful(js.Array())
  }

  private def handleRankChanges(rankings: js.Array[ContestRanking])(implicit personality: Personality, state: RobotContext): Future[Unit] = {
    (for {
      current <- rankings.find(_.username.contains(state.robotName))
      currentRank <- current.rankNum.toOption
      previous <- state.previousRankings.toOption.flatMap(_.find(_.username.contains(state.robotName)))
      previousRank <- previous.rankNum.toOption if currentRank != previousRank
      message <- personality.onRankChange(previousRank, currentRank, state.previousRankings, rankings).toOption
    } yield sendChatMessage(message)) getOrElse Future.successful({})
  }

  private def produceOrders()(implicit personality: Personality, state: RobotContext): Future[js.Array[NewOrderForm]] = {
    for {
      robotUser <- userProxy.findUserByName(state.robotName)
      portfolios <- portfolioProxy.findPortfoliosByUser(robotUser.userID_!).map(_.filter(_.closedTime.flat.isEmpty))
      buyOrders <- Future.sequence(portfolios.toList collect { case portfolio if portfolio.funds.orZero > 100 =>
        for {
          allOrders <- portfolioProxy.findOrders(portfolio.contestID_!, robotUser.userID_!).map(_.map(_.asInstanceOf[RobotOrder]))
          stocks <- findSecurities
        } yield {
          val outstandingOrders = allOrders.filter(o => o.closed.contains(0) && o.orderType.contains(BUY))
          val outstandingOrderTotal = outstandingOrders.flatMap(o => (for {price <- o.price ?? o.lastTrade; qty <- o.quantity} yield price * qty).toOption).sum
          val orderedSymbols = outstandingOrders.flatMap(_.symbol.toOption).distinct
          val filteredStocks = stocks.filterNot(_.symbol.exists(orderedSymbols.contains))
          val eligibleStocks = if (portfolio.funds.exists(_ >= outstandingOrderTotal)) filteredStocks else js.Array()
          val newOrders = produceBuyOrders(portfolio, eligibleStocks, costTarget = portfolio.funds.map(_ / eligibleStocks.length).orZero)
          if (isDebug) {
            import state.robotName
            logger.info(s"$robotName: ${stocks.size} stocks identified")
            logger.info(s"$robotName: ${orderedSymbols.size} ordered symbols [${orderedSymbols.mkString(",")}]")
            logger.info(f"$robotName: financial - funds: ${portfolio.funds.orZero}%.2f,  BUY orders: $outstandingOrderTotal%.2f")
            logger.info(s"$robotName: stocks - filtered: ${filteredStocks.size}, eligible: ${eligibleStocks.size}")
            logger.info(s"$robotName: ${newOrders.size} new orders")
            logger.info("*" * 60)
          }
          newOrders
        }
      })
      sellOrders <- Future.sequence(portfolios.toList map { portfolio =>
        for {
          positions <- portfolioProxy.findPositions(portfolio.contestID_!, robotUser.userID_!)
          allOrders <- portfolioProxy.findOrders(portfolio.contestID_!, robotUser.userID_!)
        } yield {
          val outstandingOrders = allOrders.filter(o => o.closed.contains(0) && o.orderType.contains(SELL))
          val orderedSymbols = outstandingOrders.flatMap(_.symbol.toOption).distinct
          produceSellOrders(positions, orderedSymbols)
        } toList
      })
    } yield (buyOrders ::: sellOrders).flatten.toJSArray
  }

  private def produceBuyOrders(portfolio: PortfolioLike, stocks: Seq[ResearchQuote], costTarget: Double)(implicit personality: Personality, state: RobotContext): List[NewOrderForm] = {
    case class Accumulator(orders: List[NewOrderForm] = Nil, budget: Double)
    val candidateOrders = for {
      stock <- stocks
      purchasePrice <- personality.computePurchasePrice(stock).toList
      quantity = (costTarget / purchasePrice).toInt if quantity >= 25
    } yield stock.toBuyOrder(quantity, purchasePrice)

    val results = candidateOrders.foldLeft(Accumulator(budget = portfolio.funds.orZero)) {
      case (acc@Accumulator(orders, budget), order) if order.totalCost.exists(tc => tc > 0.0 && tc <= budget) =>
        acc.copy(orders = order :: orders, budget = budget - order.totalCost.orZero)
      case (acc, _) => acc
    }
    results.orders
  }

  private def produceSellOrders(positions: js.Array[Position], orderedSymbols: js.Array[String])(implicit personality: Personality, state: RobotContext): js.Array[NewOrderForm] = {
    for {
      position <- positions.filterNot(_.symbol.exists(orderedSymbols.contains))
      salePrice <- personality.computeSalePrice(position).toList
      quantity <- position.quantity.toList
    } yield position.toSellOrder(quantity, salePrice)
  }

  private def saveOrders(orders: js.Array[NewOrderForm])(implicit personality: Personality, state: RobotContext): Future[js.Array[OrderRef]] = {
    import state._
    Future.sequence(
      for {
        order <- orders.toList
        outcome = portfolioProxy.createOrder(contestID = contestID, userID = userID, order)
      } yield outcome).map(_.toJSArray)
  }

  private def sendChatMessage(message: String)(implicit state: RobotContext): Future[Unit] = {
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
   * Robot Order Type
   */
  sealed trait RobotOrder extends OrderLike {
    def lastTrade: js.UndefOr[Double]
  }

  /**
   * Research Quote Enriched
   * @param quote the host [[ResearchQuote]]
   */
  final implicit class ResearchQuoteEnriched(val quote: ResearchQuote) extends AnyVal {
    @inline
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
    @inline
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