package com.shocktrade.server.trading.robots

import java.util.Date

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import com.ldaniels528.commons.helpers.StringHelper._
import com.ldaniels528.tabular.Tabular
import com.shocktrade.models.contest._
import com.shocktrade.models.quote.{CompleteQuote, StockQuotes}
import com.shocktrade.server.trading.robots.TradingRobot.{Invest, _}
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
 * Represents an autonomous Trading Robot
 * @author lawrence.daniels@gmail.com
 */
case class TradingRobot(name: String, strategy: TradingStrategy) extends Actor with ActorLogging {
  implicit val timeout: Timeout = 10.seconds
  private val tabular = new Tabular()

  import context.dispatcher

  override def receive = {
    case Invest => invest()

    case message =>
      log.info(s"Unhandled message: $message (${message.getClass.getName})")
      unhandled(message)
  }

  private def invest() {
    Try {
      for {
      // lookup the quotes using our trading strategy
      // db.Stocks.find({lastTrade:{$lte : 1.0}, volume:{$gte : 500000}})
        jsQuotes <- StockQuotes.findQuotes(strategy.getFilter)

        // first let's retrieve the contests I've involved in ...
        contests <- Contests.findContestsByPlayerName(name)()

      } {
        // process each contest
        contests.foreach { contest =>
          // the contest must be active and started
          if (contest.isEligible) {
            contest.participants.find(_.name == name) foreach { participant =>
              log.info(s"$name: Looking for investment opportunities for ${contest.name}...")

              // compute the funds available (subtract what we already have on order)
              val totalBuyOrdersCost = participant.orders.map(_.cost).sum
              val cashAvailable = participant.fundsAvailable - totalBuyOrdersCost
              log.info(f"$name: I've got $$$cashAvailable%.2f to spend ($$${participant.fundsAvailable}%.2f cash and $$$totalBuyOrdersCost%.2f in orders)...")
              if (cashAvailable < 500) {
                // let's filter the quotes retrieved for the strategy including the ones we've ordered or already own
                val orderedSymbols = participant.orders.map(_.symbol)
                val ownedSymbols = participant.positions.map(_.symbol)
                val orderedOrOwned = (orderedSymbols ++ ownedSymbols).distinct
                val quotes = filterQuotes(jsQuotes map (_.as[CompleteQuote])) filterNot (q => orderedOrOwned.contains(q.symbol))
                log.info(s"$name: Identified ${quotes.size} of ${jsQuotes.size} quote(s) for potential investment...")

                // let's get $1000 worth of each
                val targetSpend: BigDecimal = if (cashAvailable > MINIMUM_SPEND) MINIMUM_SPEND else cashAvailable
                val stocks = quotes map (q => Security(q.symbol, q.exchange, q.lastTrade, q.lastTrade.map(targetSpend / _).map(_.toInt), q.spreadPct, q.volume))
                tabular.transform(stocks) foreach log.info

                // how much can I buy?
                val numOfSecuritiesToBuy = (cashAvailable / targetSpend).toInt
                log.info(s"$name: I can buy up to $numOfSecuritiesToBuy securities")

                // place the orders
                implicit val timeout: Timeout = 5.seconds
                stocks.take(numOfSecuritiesToBuy) foreach { stock =>
                  createOrder(stock) foreach { order =>
                    log.info(s"$name: Creating order ${order.orderType} ${order.symbol} @ ${order.price} x ${order.quantity} ${order.priceType}")
                    Contests.createOrder(contest.id, participant.id, order)()
                  }
                }
              }
            }
          }
        }
      }
    } match {
      case Success(_) =>
      case Failure(e) =>
        log.error(s"$name: Error while attempting to invest", e)
    }
  }

  private def createOrder(stock: Security): Option[Order] = {
    for {
      exchange <- stock.exchange
      price <- stock.price
      quantity <- stock.quantity
      volume <- stock.volume
      priceType = PriceType.LIMIT
    } yield {
      Order(symbol = stock.symbol,
        exchange = exchange,
        creationTime = new Date(),
        expirationTime = Some(new DateTime().plusDays(3).toDate),
        orderType = OrderType.BUY,
        price = price,
        priceType = priceType,
        quantity = quantity,
        commission = Commissions.getCommission(priceType),
        volumeAtOrderTime = volume)
    }
  }

  private def filterQuotes(rawQuotes: Seq[CompleteQuote]) = {
    rawQuotes filter { q =>
      q.lastTrade.exists(_ > 0.0) && !q.isFinanciallyChallenged &&
        q.isChangePercentLower(-0.25) &&
        q.isSpreadIsGreater(0.30) &&
        q.isWithinPercentOfLow(0.10)
    } sortBy (_.spreadPct) reverse
  }

}

/**
 * Trading Robot Singleton
 * @author lawrence.daniels@gmail.com
 */
object TradingRobot {
  val MINIMUM_SPEND = 1000d

  implicit class CompleteQuoteExtensions(val q: CompleteQuote) extends AnyVal {

    def isChangePercentLower(minChangePct: Double): Boolean = q.changePct.exists(_ <= minChangePct)

    def isFinanciallyChallenged: Boolean = {
      val ticker = (q.symbol.lastIndexOptionOf(".") map (q.symbol.substring(0, _)) getOrElse q.symbol).toUpperCase
      ticker.length >= 5 && (ticker.last match {
        case 'A' | 'B' | 'N' | 'O' | 'P' | 'R' | 'X' => false
        case _ => true
      })
    }

    def isSpreadIsGreater(requiredSpreadPct: Double): Boolean = q.spreadPct.exists(_ > requiredSpreadPct)

    def isWithinPercentOfLow(pct: Double): Boolean = {
      (for {
        lastTrade <- q.lastTrade
        low <- q.low
        high <- q.high
      } yield lastTrade <= low * (1d + pct)).contains(true)
    }

  }

  implicit class ContestExtension(val c: Contest) extends AnyVal {

    def isEligible: Boolean = {
      c.status == ContestStatus.ACTIVE && c.startTime.exists(_.before(new Date())) && c.expirationTime.exists(_.after(new Date()))
    }
  }

  case object Invest

  case class Security(symbol: String,
                      exchange: Option[String],
                      price: Option[Double],
                      quantity: Option[Int],
                      spreadPct: Option[Double],
                      volume: Option[Long])

}
