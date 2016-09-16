package com.shocktrade.daycycle

import com.shocktrade.common.dao.contest.PortfolioUpdateDAO._
import com.shocktrade.common.dao.quotes.IntraDayQuoteData
import com.shocktrade.common.dao.quotes.IntraDayQuotesDAO._
import com.shocktrade.daycycle.IntraDayQuoteRefreshEngine._
import com.shocktrade.services.NASDAQIntraDayQuotesService
import com.shocktrade.services.NASDAQIntraDayQuotesService._
import com.shocktrade.common.models.contest.OrderLike
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.mongodb.Db
import org.scalajs.nodejs.os.OS
import org.scalajs.nodejs.{NodeRequire, console}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Intra-Day Quote Refresh Engine
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class IntraDayQuoteRefreshEngine(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) {
  private val intraDayQuoteSvc = new NASDAQIntraDayQuotesService()
  private val portfolioDAO = dbFuture.flatMap(_.getPortfolioUpdateDAO)
  private val intraDayDAO = dbFuture.flatMap(_.getIntraDayQuotesDAO)
  private val loaded = js.Dictionary[(TimeSlot, TimeSlot)]()

  // load modules
  private implicit val os = OS()
  private implicit val moment = Moment()

  /**
    * Persists intra-day quotes for all active orders to disk
    */
  def run(): Unit = {
    val startTime = js.Date.now()
    val outcome = for {
      portfolios <- portfolioDAO.flatMap(_.findActiveOrders())
      orders = portfolios.flatMap(_.orders.toOption).flatten
      results <- processQuotes(orders)
    } yield results

    // lookup the active orders
    outcome onComplete {
      case Success(results) =>
        val runTime = (js.Date.now() - startTime) / 1000
        console.log(s"Process completed in $runTime sec")
      case Failure(e) =>
        console.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
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
  private def saveQuotes(quotes: Seq[IntraDayQuoteData]) = {
    console.log(s"Saving ${quotes.size} x ${quotes.headOption.orUndefined.flatMap(_.symbol)} quotes to disk...")
    intraDayDAO.flatMap(_.saveQuotes(quotes).toFuture)
  }

}

/**
  * Intra-Day Quote Refresh Engine Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object IntraDayQuoteRefreshEngine {

  /**
    * NASDAQ Intra-Day Response Extensions
    * @param response the given [[NASDAQIntraDayResponse response]]
    */
  implicit class NASDAQIntraDayResponseExtensions(val response: NASDAQIntraDayResponse) extends AnyVal {

    @inline
    def toQuotes(implicit moment: Moment) = {
      val yyyy_mm_dd = moment(new js.Date()).format("YYYY-MM-DD")
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
          tradeDateTime = moment(yyyy_mm_dd + " " + q.time).toDate(),
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
