package com.shocktrade.webapp.vm.proccesses.cqm

import com.shocktrade.webapp.vm.opcodes._
import com.shocktrade.webapp.vm.proccesses.cqm.dao._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Contest Qualification Module
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestQualificationModule()(implicit ec: ExecutionContext) {

  def run()(implicit cqmDAO: QualificationDAO): Future[js.Array[OpCode]] = {
    for {
      ops0 <- processLimitAndMarketOrders()
      ops1 <- processOrderCloseEvents()
      ops2 <- processContestClosedEvents()
      opCodes = ops0 ++ ops1 ++ ops2
    } yield opCodes
  }

  def processLimitAndMarketOrders()(implicit cqmDAO: QualificationDAO): Future[js.Array[OpCode]] = {
    for {
      orders <- cqmDAO.findQualifiedOrders(limit = 250)
      opCodes = processOrders(orders)
    } yield opCodes
  }

  def processOrders(orders: js.Array[QualifiedOrderData]): js.Array[OpCode] = {
    for {
      order <- orders
      portfolioID <- order.portfolioID.toList
      orderID <- order.orderID.toList
      symbol <- order.symbol.toList
      exchange <- order.exchange.toList
      quantity <- order.quantity.toList
      orderType <- order.orderType.toList
      priceType <- order.priceType.toList
    } yield orderType match {
      case "BUY" => IncreasePosition(portfolioID, orderID, priceType, symbol, exchange, quantity)
      case "SELL" => DecreasePosition(portfolioID, orderID, priceType, symbol, exchange, quantity)
      case unknown => OpCodeError(s"Invalid order type '$unknown'")
    }
  }

  def processOrderCloseEvents()(implicit cqmDAO: QualificationDAO): Future[js.Array[OpCode]] = {
    cqmDAO.findExpiredOrders() map processOrderCloseEvents
  }

  def processOrderCloseEvents(orders: js.Array[OrderExpiredData]): js.Array[OpCode] = {
    for {
      order <- orders
      orderID <- order.orderID.toList
    } yield CompleteOrder(orderID, fulfilled = false, message = "Expired")
  }

  def processContestClosedEvents()(implicit cqmDAO: QualificationDAO): Future[js.Array[OpCode]] = {
    cqmDAO.findExpiredContests() map processContestClosedEvents
  }

  def processContestClosedEvents(contests: js.Array[ContestExpiredData]): js.Array[OpCode] = {
    val opCodes = for {
      contest <- contests
      portfolioID <- contest.portfolioID.toList
    } yield LiquidatePortfolio(portfolioID)

    opCodes ++ contests.headOption.flatMap(_.contestID.toOption).map(CloseContest.apply).toJSArray
  }

}
