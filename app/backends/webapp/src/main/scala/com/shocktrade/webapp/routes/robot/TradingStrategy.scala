package com.shocktrade.webapp.routes.robot

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.webapp.routes.contest.OrderTypes.OrderType
import com.shocktrade.webapp.routes.contest.PriceTypes.PriceType
import com.shocktrade.webapp.routes.contest.dao.OrderData
import com.shocktrade.webapp.routes.dao._
import com.shocktrade.webapp.routes.robot.dao.RobotData
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.Random

/**
 * Represents a Trading Strategy
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait TradingStrategy {
  private val random = new Random()

  /**
   * Allows the robot to act autonomously
   * @param robot the given [[RobotData]]
   */
  def operate(robot: RobotData): Future[Seq[RobotActivity]] = {
    implicit val _robot: RobotData = robot

    val activities = Seq(buySecurities _, sellSecurities _)
    for {
      responses <- Future.sequence(activities.map(_.apply()))
      _ <- Future.sequence(responses.map(chatAboutIt)).map(_.sum)
    } yield responses
  }

  def buySecurities()(implicit robot: RobotData): Future[OrderBuyActivity]

  def chatAboutIt(response: RobotActivity)(implicit robot: RobotData): Future[Int] = {
    val message_? = response match {
      case JoinedContestActivity(_, _, counts) if counts > 0 =>
        random.nextInt(4) match {
          case 0 => Some("You know you're in trouble now...")
          case 1 => Some("Don't mind me, just playing around...")
          case 2 => Some("No offense but... I got this!")
          case _ => Some("What's up everybody")
        }
      case OrderBuyActivity(_, _, orders, counts) if counts > 0 =>
        random.nextInt(3) match {
          case 0 => Some(s"I just placed ${orders.size} buy orders.")
          case 1 => Some("Just bought some stocks...")
          case _ => Some("You're so not ready for what's coming!")
        }
      case OrderSellActivity(_, _, orders, counts) if counts > 0 =>
        random.nextInt(3) match {
          case 0 => Some(s"I just placed ${orders.size} sell orders.")
          case 1 => Some("I just made a huge profit!")
          case 2 => Some("Just sold some stocks...")
          case _ => Some("You're so not ready for what's coming!")
        }
      case _ => None
    }
    message_?.map(sendChat).getOrElse(Future.successful(0))
  }

  def findRobotRef(implicit robot: RobotData): Future[RobotRef] = Future.successful(RobotRef(robot))

  def findStocksToBuy(options: ResearchOptions)(implicit robot: RobotData): Future[js.Array[ResearchQuote]] = {
    for {
      RobotRef(_, robotName, _, portfolioID) <- findRobotRef
      stocks <- researchDAO.research(options)
      pendingOrderedSymbols <- robotDAO.findPendingOrderTickers(robotName, portfolioID)
    } yield {
      stocks
        .filterNot(s => pendingOrderedSymbols.flatMap(_.symbol.toOption).contains(s.symbol.orNull))
        .sortBy(r => (-r.spread.orZero, r.lastTrade.orZero, -r.volume.orZero))
    }
  }

  def findStocksToSell(options: ResearchOptions)(implicit robot: RobotData): Future[js.Array[ResearchQuote]] = {
    // TODO finish me!
    Future.successful(js.Array())
  }

  def saveOrders(orders: Seq[OrderData])(implicit robot: RobotData): Future[Int] = {
    for {
      RobotRef(_, _, _, portfolioID) <- findRobotRef
      counts <- Future.sequence(orders.map(o => portfolioDAO.createOrder(portfolioID, o))).map(_.sum)
    } yield counts
  }

  def sellSecurities()(implicit robot: RobotData): Future[OrderSellActivity]

  def sendChat(message: String)(implicit robot: RobotData): Future[Int] = {
    for {
      RobotRef(robotUserID, _, contestID, _) <- findRobotRef
      count <- contestDAO.addChatMessage(contestID, robotUserID, message)
    } yield count
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

    def toOrder(quantity: Double,
                orderType: OrderType,
                priceType: PriceType,
                price: js.UndefOr[Double] = js.undefined): OrderData = {
      new OrderData(
        symbol = quote.symbol,
        exchange = quote.symbol,
        orderType = orderType,
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