package com.shocktrade.webapp.vm.proccesses.cqm

import com.shocktrade.common.Commissions
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.webapp.vm.opcodes._
import com.shocktrade.webapp.vm.proccesses.cqm.dao._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Contest Qualification Module
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestQualificationModule()(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private implicit val clock: TradingClock = new TradingClock()

  def run()(implicit cqmDAO: QualificationDAO): Future[js.Array[OpCode]] = {
    for {
      ops0 <- processLimitAndMarketOrders()
      ops1 <- processOrderCloseEvents()
      ops2 <- processContestClosedEvents()
    } yield ops0 ++ ops1 ++ ops2
  }

  def processLimitAndMarketOrders()(implicit cqmDAO: QualificationDAO): Future[js.Array[OpCode]] = {
    cqmDAO.findQualifiedOrders(limit = 250) map processOrders
  }

  def processOrders(orders: js.Array[QualifiedOrderData]): js.Array[OpCode] = {
    if (orders.nonEmpty) logger.info(s"processing ${orders.size} qualified order event(s)")
    for {
      order <- orders
      portfolioID <- order.portfolioID.toList
      orderID <- order.orderID.toList
      symbol <- order.symbol.toList
      exchange <- order.exchange.toList
      lastTrade <- order.lastTrade.toList
      quantity <- order.quantity.toList
      orderType <- order.orderType.toList
      priceType <- order.priceType.toList
    } yield {
      val commission = Commissions.getCommission(priceType)
      orderType match {
        case "BUY" => IncreasePosition(portfolioID, orderID, symbol, exchange, quantity, cost = lastTrade * quantity + commission)
        case "SELL" => DecreasePosition(portfolioID, orderID, symbol, exchange, quantity, proceeds = lastTrade * quantity - commission)
        case unknown => OpCodeError(s"Invalid order type '$unknown'")
      }
    }
  }

  def processOrderCloseEvents()(implicit cqmDAO: QualificationDAO): Future[js.Array[OpCode]] = {
    cqmDAO.findExpiredOrders() map processOrderCloseEvents
  }

  def processOrderCloseEvents(orders: js.Array[OrderExpiredData]): js.Array[OpCode] = {
    if (orders.nonEmpty) logger.info(s"processing ${orders.size} order close event(s)")
    for {
      order <- orders
      orderID <- order.orderID.toList
    } yield CompleteOrder(orderID, fulfilled = false, message = "Expired")
  }

  def processContestClosedEvents()(implicit cqmDAO: QualificationDAO): Future[js.Array[OpCode]] = {
    cqmDAO.findExpiredContests() map processContestClosedEvents
  }

  def processContestClosedEvents(contests: js.Array[ContestExpiredData]): js.Array[OpCode] = {
    if (contests.nonEmpty) logger.info(s"processing ${contests.size} contest close event(s)")
    for {
      contest <- contests
      contestID <- contest.contestID.toList
    } yield CloseContest(contestID)
  }

}
