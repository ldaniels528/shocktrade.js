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
import scala.scalajs.js.JSConverters._
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
      start() onComplete {
        case Success(result) =>
          logger.info(s"CQM: closed = ${result.closedCount}, positions = ${result.positionCount}, updatedOrders = ${result.updatedOrderCount}")

          // capture the time as the last run time
          lastRun = startTime
        case Failure(e) =>
          logger.error(s"CQM failure: ${e.getMessage}", e)
          e.printStackTrace()

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
  def start(): Future[CqmResponse] = {
    for {
      closedCount <- processContestClosing()
      (buyPositions, buyOrders, buyPositionCount, buyOrderCount) <- processBuyFlow()
      (sellPositions, sellOrders, sellPositionCount, sellOrderCount) <- processSellFlow()
    } yield new CqmResponse(
      positions = (buyPositions ::: sellPositions).toJSArray,
      updatedOrders = (buyOrders ::: sellOrders).toJSArray,
      closedCount = closedCount,
      positionCount = buyPositionCount + sellPositionCount,
      updatedOrderCount = buyOrderCount + sellOrderCount
    )
  }

  ///////////////////////////////////////////////////////////////////////
  //  Contest Close Flow
  ///////////////////////////////////////////////////////////////////////

  private def processContestClosing(): Future[Int] = {
    qualificationDAO.closeOutExpiredContests() recover {
      case e: Throwable =>
        logger.error(s"Failure in Contest Closing flow: ${e.getMessage}")
        e.printStackTrace()
        0
    }
  }

  ///////////////////////////////////////////////////////////////////////
  //  Order BUY Flow
  ///////////////////////////////////////////////////////////////////////

  private def processBuyFlow(): Future[(List[PositionData], List[OrderData], Int, Int)] = {
    val outcome = for {
      qualifiedOrders <- qualificationDAO.findQualifiedBuyOrders(limit = 1000)
      preparedOrders = prepareBuyOrders(qualifiedOrders)
      newPositions = preparedOrders collect { case Left(position) => position }
      updatedOrders = preparedOrders collect { case Right(order) => order }
      positionCount <- Future.sequence(newPositions.map(qualificationDAO.createPosition)).map(_.sum)
      updatedOrdersCount <- Future.sequence(updatedOrders.map(qualificationDAO.updateOrder)).map(_.sum)
    } yield (newPositions, updatedOrders, positionCount, updatedOrdersCount)

    outcome recover {
      case e: Throwable =>
        logger.error("Failure in BUY flow:")
        e.printStackTrace()
        (Nil, Nil, 0, 0)
    }
  }

  private def prepareBuyOrders(qualifiedOrders: js.Array[QualifiedOrderData]): List[Either[PositionData, OrderData]] = {
    val processedTime = new js.Date()
    case class Accumulator(budget: Double, positions: List[PositionData] = Nil, updatedOrders: List[OrderData] = Nil)
    val results = qualifiedOrders.groupBy(_.userID) flatMap { case (userID_?, orders) =>
      for {
        userID <- userID_?.toOption
        funds <- orders.headOption.flatMap(_.funds.toOption) if funds > 0.0
      } yield {
        orders.foldLeft(Accumulator(budget = funds)) {
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

  ///////////////////////////////////////////////////////////////////////
  //  Order SELL Flow
  ///////////////////////////////////////////////////////////////////////

  private def processSellFlow(): Future[(List[PositionData], List[OrderData], Int, Int)] = {
    val outcome = for {
      qualifiedOrders <- qualificationDAO.findQualifiedSellOrders(limit = 1000)
      preparedOrders = prepareBuyOrders(qualifiedOrders)
      updatedPositions = preparedOrders collect { case Left(position) => position }
      updatedOrders = preparedOrders collect { case Right(order) => order }
      updatedPositionCount = 0
      updatedOrdersCount = 0
    } yield (updatedPositions, updatedOrders, updatedPositionCount, updatedOrdersCount)

    outcome recover {
      case e: Throwable =>
        logger.error("Failure in SELL flow:")
        e.printStackTrace()
        (Nil, Nil, 0, 0)
    }
  }

  private def prepareSellOrders(qualifiedOrders: js.Array[QualifiedOrderData]): List[Either[PositionData, OrderData]] = {
    val processedTime = new js.Date()

    Nil
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
        price = order.lastTrade,
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
        cost = for {price <- order.lastTrade; quantity <- order.quantity} yield price * quantity + commission,
        netValue = for {price <- order.lastTrade; quantity <- order.quantity} yield price * quantity,
        commission = commission,
        processedTime = processedTime)
    }
  }

}