package com.shocktrade.server.trading.robots

import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import com.ldaniels528.commons.helpers.OptionHelper._
import com.ldaniels528.commons.helpers.StringHelper._
import com.ldaniels528.tabular.Tabular
import com.shocktrade.models.contest.PerkTypes.PerkType
import com.shocktrade.models.contest._
import com.shocktrade.models.profile.{UserProfile, UserProfiles}
import com.shocktrade.models.quote.{CompleteQuote, StockQuotes}
import com.shocktrade.server.trading.robots.TradingRobot.{Invest, _}
import com.shocktrade.server.trading.{ContestDAO, Contests}
import org.joda.time.DateTime
import play.api.libs.json.JsObject

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * Represents an autonomous Trading Robot
 * @author lawrence.daniels@gmail.com
 */
case class TradingRobot(name: String, strategy: TradingStrategy) extends Actor with ActorLogging {
  implicit val timeout: Timeout = 10.seconds
  private val processing = new AtomicBoolean(false)
  private val tabular = new Tabular()

  import context.dispatcher

  override def receive = {
    case Invest =>
      if (processing.compareAndSet(false, true)) {
        log.info(s"$name is attempting to invest...")
        invest() onComplete {
          case Success(_) => processing.set(false)
          case Failure(e) =>
            processing.set(false)
            log.error(s"Robot $name failed during investment operations", e)
        }
      }

    case message =>
      log.info(s"Unhandled message: $message (${message.getClass.getName})")
      unhandled(message)
  }

  private def invest() = {
    for {
    // lookup the robot's user profile
      profile <- UserProfiles.findProfileByName(name) map (_ orDie s"The user profile for robot $name could not be found")

      // find contests to join
      _ <- findContestsToJoin(profile)

      // lookup the quotes using our trading strategy
      jsQuotes <- StockQuotes.findQuotes(strategy.getFilter)

      // first let's retrieve the contests I've involved in ...
      contests <- Contests.findContestsByPlayerName(name)()
    } yield {
      // process each contest
      contests.flatMap { contest =>
        // the contest must be active and started
        if (contest.isEligible) {
          //log.info(s"jsQuotes = ${Json.prettyPrint(jsQuotes.head)}")
          contest.participants.find(_.name == name) foreach (operateRobot(contest, _, jsQuotes))
          Some(contest)
        }
        else None
      }
    }
  }

  private def operateRobot(contest: Contest, participant: Participant, jsQuotes: Seq[JsObject]) = {
    log.info(s"$name: Looking for investment opportunities in '${contest.name}'...")

    // compute the funds available (subtract what we already have on order)
    val totalBuyOrdersCost = participant.orders.map(_.cost).sum
    val cashAvailable = participant.cashAccount.cashFunds - totalBuyOrdersCost
    log.info(f"$name: I've got $$$cashAvailable%.2f to spend ($$${participant.cashAccount.cashFunds}%.2f cash and $$$totalBuyOrdersCost%.2f in orders)...")
    if (cashAvailable > 500) {
      // let's filter the quotes retrieved for the strategy including the ones we've ordered or already own
      val orderedSymbols = participant.orders.map(_.symbol)
      val ownedSymbols = participant.positions.map(_.symbol)
      val orderedOrOwned = (orderedSymbols ++ ownedSymbols).distinct
      val quotes = jsQuotes map (_.as[CompleteQuote]) filterNot (q => orderedOrOwned.contains(q.symbol))
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
          Contests.createOrder(contest.id, participant.id, order)
        }
      }
    }
  }

  private def findContestsToJoin(u: UserProfile) = {
    log.info(s"$name is looking for contests to join...")
    ContestDAO.findContests(SearchOptions(activeOnly = Some(true))) map { contests =>
      contests.foreach { contest =>
        log.info(s"$name is considering joining '${contest.name}'...")
        // if robots are allowed, and I have not already joined ...
        if (contest.robotsAllowed && !contest.participants.exists(_.id == u.id)) {
          log.info(s"$name: Joining '${contest.name}' ...")
          for {
          // join the contest
            contest_? <- Contests.joinContest(contest.id, Participant(id = u.id, u.name, u.facebookID, cashAccount = CashAccount(cashFunds = contest.startingBalance)))

            // always purchase the 'Fee Waiver' Perk
            _ = for {
              c <- contest_?
              p <- c.participants.find(_.name == name)
            } yield ensurePerk(c, p, PerkTypes.FEEWAIVR)

          } yield contest_?
        }
      }
    }
  }

  private def createOrder(stock: Security): Option[Order] = {
    for {
      exchange <- stock.exchange
      price <- stock.price
      quantity <- stock.quantity
      volume <- stock.volume
      priceType = PriceTypes.LIMIT
    } yield {
      Order(
        accountType = AccountTypes.CASH, // TODO robots can also buy on Margin ...
        symbol = stock.symbol,
        exchange = exchange,
        creationTime = new DateTime().minusDays(4).toDate, // TODO remove this after testing
        orderTerm = OrderTerms.GOOD_FOR_3_DAYS,
        orderType = OrderTypes.BUY,
        price = price,
        priceType = priceType,
        quantity = quantity,
        commission = Commissions.getCommission(priceType))
    }
  }

  private def ensurePerk(contest: Contest, participant: Participant, perkType: PerkType) = {
    if (!contest.perksAllowed || participant.perks.contains(perkType)) Future.successful(None)
    else {
      log.info(s"$name: Attempting to purchase perk $perkType [${contest.name}}]...")
      for {
        perks <- Contests.findAvailablePerks(contest.id)
        feeWaiverPerk = perks.find(_.code == perkType) orDie s"Perk $perkType not found"
        updated <- Contests.purchasePerks(contest.id, participant.id, Seq(perkType), feeWaiverPerk.cost)
      } yield updated
    }
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
      c.status == ContestStatuses.ACTIVE && c.startTime.exists(_.before(new Date())) && c.expirationTime.exists(_.after(new Date()))
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
