package com.shocktrade.qualification

import com.shocktrade.common.models.contest.OrderLike
import com.shocktrade.qualification.IntraDayQuoteDaemon._
import com.shocktrade.server.common.TradingClock
import com.shocktrade.server.concurrent.Daemon
import com.shocktrade.server.services.NASDAQIntraDayQuotesService
import com.shocktrade.server.services.NASDAQIntraDayQuotesService._
import io.scalajs.nodejs.console
import io.scalajs.npm.moment.Moment
import io.scalajs.npm.mongodb.{BulkWriteOpResultObject, Db}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

/**
  * Intra-Day Quote Daemon (NASDAQ Datafeed)
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class IntraDayQuoteDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext) extends Daemon[Seq[BulkWriteOpResultObject]] {
  // get DAO and service references
  private val intraDayQuoteSvc = new NASDAQIntraDayQuotesService()
  //private val portfolioDAO = dbFuture.map(_.getPortfolioUpdateDAO)
  //private val intraDayDAO = dbFuture.map(_.getIntraDayQuotesDAO)

  // internal fields
  private val loaded = js.Dictionary[(TimeSlot, TimeSlot)]()
  private var lastRun: js.Date = new js.Date()

  /**
    * Indicates whether the daemon is eligible to be executed
    * @param tradingClock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  override def isReady(tradingClock: TradingClock): Boolean = tradingClock.isTradingActive || tradingClock.isTradingActive(lastRun)

  /**
    * Persists intra-day quotes for all active orders to disk
    * @param tradingClock the given [[TradingClock trading clock]]
    */
  override def run(tradingClock: TradingClock): Future[Seq[BulkWriteOpResultObject]] = {
    /*
    val startTime = js.Date.now()
    val outcome = for {
      portfolios <- portfolioDAO.flatMap(_.findActiveOrders())
      orders = portfolios.flatMap(_.orders.toOption).flatten
      results <- processQuotes(orders)
    } yield results

    // lookup the active orders
    outcome onComplete {
      case Success(results) =>
        lastRun = new js.Date(startTime)
        val runTime = (js.Date.now() - startTime) / 1000
        console.log(s"Process completed in $runTime sec")
      case Failure(e) =>
        console.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome*/
    ???
  }

  /**
    * Processes intra-day quotes for the given collection of orders
    * @param orders the given orders
    * @return the promise of the collection of write results
    */
  private def processQuotes(orders: Seq[OrderLike]) = {
    val symbols = orders.filterNot(_.isMarketAtCloseOrder).flatMap(_.symbol.toOption).distinct
    Future.sequence {
      symbols flatMap { symbol =>
        val startTimeSlot = getStartTimeSlot(symbol)
        val stopTimeSlot = getStopTimeSlot(symbol)

        // gather all of the responses for each time index start with page 1
        console.log(s"Retrieving intra-day quotes for $symbol from '${intraDayQuoteSvc.getTimeSlotText(startTimeSlot)}' to '${intraDayQuoteSvc.getTimeSlotText(stopTimeSlot)}'")
        startTimeSlot to stopTimeSlot map { timeSlot =>
          for {
            response <- intraDayQuoteSvc(symbol, timeSlot)
            writeResults <- saveQuotes(response.toQuotes)
          } yield writeResults
        }
      }
    }
  }

  @inline
  private def getStartTimeSlot(symbol: String): TimeSlot = {
    val (start, end) = loaded.getOrElseUpdate(symbol, (ET_0930_TO_0959, intraDayQuoteSvc.getTimeSlot(new js.Date())))
    start
  }

  @inline
  private def getStopTimeSlot(symbol: String): TimeSlot = {
    intraDayQuoteSvc.getTimeSlot(new js.Date())
  }

  @inline
  private def saveQuotes(quotes: Seq[IntraDayQuoteData]): Future[js.Object] = ???

}

/**
  * Intra-Day Quote Refresh Engine Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object IntraDayQuoteDaemon {

  /**
   * Intra-Day Quote Data
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class IntraDayQuoteData(val _id: js.UndefOr[String] = js.undefined,
                          val symbol: js.UndefOr[String],
                          val price: js.UndefOr[Double],
                          val time: js.UndefOr[String],
                          val volume: js.UndefOr[Double],
                          var aggregateVolume: js.UndefOr[Double],
                          val tradeDateTime: js.UndefOr[js.Date],
                          val creationTime: js.Date = new js.Date()) extends js.Object

  /**
    * NASDAQ Intra-Day Response Extensions
    * @param response the given [[NASDAQIntraDayResponse response]]
    */
  implicit class NASDAQIntraDayResponseExtensions(val response: NASDAQIntraDayResponse) extends AnyVal {

    @inline
    def toQuotes: js.Array[IntraDayQuoteData] = {
      val yyyy_mm_dd = Moment(new js.Date()).format("YYYY-MM-DD")
      val rawQuotes = for {
        page <- response.pages
        data <- page.quotes.groupBy(q => (q.time.orNull, q.price ?> 0)) map { case ((time, price), items) =>
          RawQuote(symbol = response.symbol, price = price, time = time, volume = items.map(_.volume ?> 0d).sum)
        }
      } yield data

      console.log(s"${response.symbol}: ${response.pages.length} page(s), ${rawQuotes.length} quote(s), ${response.pages.flatMap(_.quotes).length} item(s)")

      // combine quotes that occur during the same time with the same price
      var aggregateVolume = 0d
      rawQuotes.map { q =>
        aggregateVolume += q.volume
        new IntraDayQuoteData(
          symbol = q.symbol,
          price = q.price,
          time = q.time,
          tradeDateTime = Moment(yyyy_mm_dd + " " + q.time).toDate(),
          volume = q.volume,
          aggregateVolume = aggregateVolume
        )
      }
    }

    @inline
    private def extractUrlAttributes(pageUrl: String): js.Dictionary[String] = {
      pageUrl.indexOf("?") match {
        case -1 => js.Dictionary()
        case index =>
          js.Dictionary(pageUrl.substring(index + 1).split("[&]").flatMap(_.split("[=]") match {
            case Array(key, value) => Some(key -> value)
            case _ => None
          }): _*)
      }
    }

  }

  case class RawQuote(symbol: String, price: Double, time: String, volume: Double)

  /**
    * UndefOr Extensions
    * @param aValue the given optional value
    * @tparam T the value type
    */
  implicit class MyUndefOrExtensions[T](val aValue: js.UndefOr[T]) extends AnyVal {

    @inline
    def ?>(defaultValue: T): T = aValue getOrElse defaultValue

  }

}
