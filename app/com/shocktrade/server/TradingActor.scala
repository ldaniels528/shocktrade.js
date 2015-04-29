package com.shocktrade.server

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import com.shocktrade.models.contest._
import com.shocktrade.server.TradingActor._
import com.shocktrade.util.DateUtil._
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Trading Actor
 * @author lawrence.daniels@gmail.com
 */
class TradingActor() extends Actor with ActorLogging {
  private implicit val timeout: Timeout = 30.second
  private var lastMarketClose: Option[DateTime] = None

  import context.dispatcher

  override def receive = {
    case ProcessOrders(contest, lockExpirationTime, asOfDate) =>
      processOrders(contest, lockExpirationTime, asOfDate)
    case message =>
      unhandled(message)
  }

  /**
   * Process all active orders
   * @param contest the given contest
   * @param asOfDate the given effective date
   */
  private def processOrders(contest: Contest, lockExpirationTime: DateTime, asOfDate: DateTime) {
    // if trading was active during the as-of date
    if (isTradingActive(asOfDate)) {
      // get the active orders
      val orders = getActiveOrders(contest)
        .filterNot(_.order.priceType == PriceType.MARKET_ON_CLOSE)
        .groupBy(_.order.priceType)

      if (orders.nonEmpty) {
        log.info(s"${contest.name} [LIMIT/MARKET]: Found ${orders.size} orders...")

        // process limit and market orders
        orders.get(PriceType.LIMIT) foreach processLimitOrders
        orders.get(PriceType.MARKET) foreach processMarketOrders
      }
    }

    // only process market on close orders after market close
    else if (lastMarketClose.isEmpty || lastMarketClose.exists(_.toDate.before(asOfDate))) {
      val orders = getActiveOrders(contest) filter (_.order.priceType == PriceType.MARKET_ON_CLOSE)
      if (orders.nonEmpty) {
        log.info(s"${contest.name} [MARKET CLOSE]: Found ${orders.size} orders...")

        processMarketAtCloseOrders(orders)
        lastMarketClose = Some(new DateTime(getNextTradeStartTime))
      }
    }

    // close all expired orders
    TradingDAO.closeExpiredOrders(contest, asOfDate)

    // finally unlock the contest
    TradingDAO.unlockContest(contest.id, lockExpirationTime) onComplete {
      case Failure(e) =>
        log.error(e, s"Failed while attempting to unlock Contest '${contest.name}'")
      case Success(_) =>
    }
  }

  private def processLimitOrders(orders: List[ActiveOrder]): Unit = {

  }

  private def processMarketOrders(orders: List[ActiveOrder]): Unit = {

  }

  private def processMarketAtCloseOrders(orders: List[ActiveOrder]): Unit = {

  }

  private def getActiveOrders(contest: Contest) = {
    contest.participants flatMap { participant =>
      participant.orders map (o => ActiveOrder(contest, participant, o))
    }
  }

}

/**
 * Trading Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object TradingActor {

  case class ActiveOrder(contest: Contest, participant: Participant, order: Order)

  case class ProcessOrders(contest: Contest, lockExpirationTime: DateTime, asOfDate: DateTime)

}
