package com.shocktrade.webapp.routes
package qualification

import com.shocktrade.common.Commissions
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.webapp.routes.contest.dao.{OrderData, PositionData}
import com.shocktrade.webapp.routes.qualification.ContestQualificationModule._
import com.shocktrade.webapp.routes.qualification.dao.{QualificationDAO, QualifiedOrderData}
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Contest Qualification Module (CQM)
 * @param ec    the implicit [[ExecutionContext]]
 * @param clock the implicit [[TradingClock trading clock]]
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestQualificationModule(implicit ec: ExecutionContext, clock: TradingClock, qualificationDAO: QualificationDAO) {
  // get DAO and service references
  private val logger = LoggerFactory.getLogger(getClass)

  // internal fields
  private var lastRun: js.Date = new js.Date()

  /**
   * Executes the process
   */
  def run(): Unit = {
    if (isReady || clock.isTradingActive(lastRun)) {
      logger.info("Starting the Contest Qualification Module...")

      // capture the time as the last run time
      val startTime = new js.Date()

      // perform the qualification
      start(marketClosed = clock.isTradingActive(lastRun)) onComplete {
        case Success((_, _, positionCount, updatedOrderCount)) =>
          logger.info(s"CQM: positionCount = $positionCount, updatedOrderCount = $updatedOrderCount")

          // capture the time as the last run time
          lastRun = startTime
        case Failure(e) =>
          logger.error(s"CQM failure: ${e.getMessage}", e)

          // capture the time as the last run time
          lastRun = startTime
      }
    }
  }

  /**
   * Indicates whether the daemon is eligible to be executed
   * @return true, if the daemon is eligible to be executed
   */
  def isReady: Boolean = clock.isTradingActive || clock.isTradingActive(lastRun)

  /**
   * Executes the process
   */
  def start(marketClosed: Boolean): Future[(List[PositionData], List[OrderData], Int, Int)] = {
    for {
      qualifiedOrders <- qualificationDAO.findQualifiedOrders(limit = 100)
      processedOrders = processOrders(qualifiedOrders)
      newPositions = processedOrders collect { case Left(position) => position }
      updatedOrders = processedOrders collect { case Right(order) => order }
      positionCount <- Future.sequence(newPositions.map(qualificationDAO.createPosition)).map(_.sum)
      rejectedCount <- Future.sequence(updatedOrders.map(qualificationDAO.updateOrder)).map(_.sum)
    } yield (newPositions, updatedOrders, positionCount, rejectedCount)
  }

  private def processOrders(qualifiedOrders: js.Array[QualifiedOrderData]): List[Either[PositionData, OrderData]] = {
    val processedTime = new js.Date()

    // process the orders
    case class Accumulator(budget: Double, positions: List[PositionData] = Nil, updatedOrders: List[OrderData] = Nil)
    val results = qualifiedOrders.groupBy(_.userID) flatMap { case (userID_?, orders) =>
      for {
        userID <- userID_?.toOption
        funds <- orders.headOption.flatMap(_.funds.toOption) if funds > 0.0
      } yield {
        orders.foldLeft[Accumulator](Accumulator(budget = funds)) {
          case (acc@Accumulator(budget, positions, updatedOrders), order) if order.cost.exists(cost => budget >= cost && cost > 0) =>
            acc.copy(
              budget = budget - order.cost.orZero,
              positions = order.toPosition(processedTime) :: positions,
              updatedOrders = order.toFulfilledOrder(processedTime) :: updatedOrders
            )
          case (acc@Accumulator(_, _, updatedOrders), order) =>
            acc.copy(updatedOrders = order.toFailedOrder(message = "Insufficient funds", processedTime) :: updatedOrders)
        }
      }
    } toList

    // return a collection containing either a position or a failed order
    results.flatMap(_.positions.map(Left.apply)) ::: results.flatMap(_.updatedOrders.map(Right.apply))
  }

}

/**
 * Contest Qualification Module Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestQualificationModule {

  /**
   * Qualified Order Data Enriched
   * @param order the given [[QualifiedOrderData]]
   */
  final implicit class QualifiedOrderDataEnriched(val order: QualifiedOrderData) extends AnyVal {

    def toFulfilledOrder(processedTime: js.Date): OrderData = {
      new OrderData(
        orderID = order.orderID,
        symbol = order.symbol,
        exchange = order.exchange,
        orderType = order.orderType,
        priceType = order.priceType,
        price = order.price,
        quantity = order.quantity,
        creationTime = order.creationTime,
        expirationTime = order.expirationTime,
        processedTime = processedTime,
        message = "Executed",
        fulfilled = true,
        closed = true)
    }

    def toFailedOrder(message: String, processedTime: js.Date): OrderData = {
      new OrderData(
        orderID = order.orderID,
        symbol = order.symbol,
        exchange = order.exchange,
        orderType = order.orderType,
        priceType = order.priceType,
        price = order.price,
        quantity = order.quantity,
        creationTime = order.creationTime,
        expirationTime = order.expirationTime,
        processedTime = processedTime,
        message = message,
        fulfilled = false,
        closed = true)
    }

    def toPosition(processedTime: js.Date): PositionData = {
      val commission = Commissions.getCommission(order.priceType)
      new PositionData(
        positionID = js.undefined,
        portfolioID = order.portfolioID,
        orderID = order.orderID,
        symbol = order.symbol,
        exchange = order.exchange,
        price = order.lastTrade,
        quantity = order.quantity,
        tradeDateTime = order.tradeDateTime,
        cost = for {price <- order.price; quantity <- order.quantity} yield price * quantity + commission,
        netValue = for {price <- order.price; quantity <- order.quantity} yield price * quantity,
        commission = commission,
        processedTime = processedTime)
    }
  }

}