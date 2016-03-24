package com.shocktrade.models.quote

import akka.actor.Props
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.github.ldaniels528.commons.helpers.OptionHelper._
import com.shocktrade.actors.QuoteMessages._
import com.shocktrade.actors.WebSockets.QuoteUpdated
import com.shocktrade.actors.{DBaseQuoteActor, RealTimeQuoteActor, WebSockets}
import com.shocktrade.controllers.QuotesController._
import com.shocktrade.util.BSONHelper._
import com.shocktrade.util.{ConcurrentCache, DateUtil}
import org.joda.time.DateTime
import play.libs.Akka
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONArray, BSONDocument => BS, BSONDocumentReader}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 * Stock Quote Proxy
 * @author lawrence.daniels@gmail.com
 */
object StockQuotes {
  private val realTimeCache = ConcurrentCache[String, BS](1.minute)
  private val diskCache = ConcurrentCache[String, BS](4.hours)
  private val system = Akka.system
  private val quoteActor = system.actorOf(Props[RealTimeQuoteActor].withRouter(RoundRobinPool(nrOfInstances = 50)), name = "QuoteRealTime")
  private val mongoReader = system.actorOf(Props[DBaseQuoteActor].withRouter(RoundRobinPool(nrOfInstances = 50)), name = "QuoteReader")
  private val mongoWriter = system.actorOf(Props[DBaseQuoteActor], name = "QuoteWriter")
  private lazy val mcQBS = db.collection[BSONCollection]("Stocks")
  implicit val timeout: Timeout = 45.seconds

  import system.dispatcher

  def findQuotes(filter: QuoteFilter) = {
    (mongoReader ? FindQuotes(filter)) map {
      case e: Throwable => throw new IllegalStateException(e)
      case response => response.asInstanceOf[Seq[BS]]
    }
  }

  /**
   * Retrieves a real-time quote for the given symbol
   * @param symbol the given symbol (e.g. 'AAPL')
   * @return a promise of an option of a [[reactivemongo.bson.BSONDocument quote]]
   */
  def findRealTimeQuote(symbol: String) = {

    def relayQuote(task: Future[Option[BS]]) = {
      task.foreach(_ foreach { quote =>
        realTimeCache.put(symbol, quote, if (DateUtil.isTradingActive) 1.minute else 15.minute)
        WebSockets ! QuoteUpdated(quote)
        mongoWriter ! SaveQuote(symbol, quote)
      })
      task
    }

    val mySymbol = symbol.toUpperCase.trim
    if (DateUtil.isTradingActive) relayQuote(findRealTimeQuoteFromService(mySymbol))
    else
      realTimeCache.get(mySymbol) match {
        case quote@Some(_) => Future.successful(quote)
        case None =>
          relayQuote(findRealTimeQuoteFromService(mySymbol))
      }
  }

  def findRealTimeQuotes(symbols: Seq[String]) = {
    val quotes = Future.sequence(symbols map findRealTimeQuote)
    quotes.map(_.flatten)
  }

  def findRealTimeQuoteFromService(symbol: String) = {
    (quoteActor ? GetQuote(symbol)).mapTo[Option[BS]]
  }

  /**
   * Retrieves a database quote for the given symbol
   * @param symbol the given symbol (e.g. 'AAPL')
   * @return a promise of an option of a [[reactivemongo.bson.BSONDocument quote]]
   */
  def findDBaseQuote(symbol: String) = {
    val mySymbol = symbol.toUpperCase.trim
    diskCache.get(mySymbol) match {
      case quote@Some(_) => Future.successful(quote)
      case None =>
        val quote = (mongoReader ? GetQuote(mySymbol)).mapTo[Option[BS]]
        quote.foreach(_ foreach (diskCache.put(mySymbol, _)))
        quote
    }
  }

  def findDBaseFullQuote(symbol: String) = {
    (mongoReader ? GetFullQuote(symbol)).mapTo[Option[BS]]
  }

  /**
   * Retrieves a database quote for the given symbol
   * @param symbols the given collection of symbols (e.g. 'AAPL', 'AMD')
   * @return a promise of an option of a [[reactivemongo.bson.BSONDocument quote]]
   */
  def findDBaseQuotes(symbols: Seq[String]) = {
    // first, get as many of the quote from the cache as we can
    val cachedQuotes = symbols flatMap diskCache.get
    val remainingSymbols = symbols.filterNot(diskCache.contains)
    if (remainingSymbols.isEmpty) Future.successful(cachedQuotes)
    else {
      // query any remaining quotes from disk
      val task = (mongoReader ? GetQuotes(remainingSymbols)).mapTo[Seq[BS]]
      task.foreach {
        _ foreach { quote =>
          quote.getAs[String]("symbol").foreach(diskCache.put(_, quote))
        }
      }
      task
    }
  }

  /**
   * Retrieves a complete quote; the composition of real-time quote and a disc-based quote
   * @param symbol the given ticker symbol
   * @return the [[Future promise]] of an option of a [[reactivemongo.bson.BSONDocument quote]]
   */
  def findFullQuote(symbol: String) = {
    val mySymbol = symbol.toUpperCase.trim
    val rtQuoteFuture = findRealTimeQuote(mySymbol)
    val dbQuoteFuture = findDBaseFullQuote(mySymbol)
    for {
      rtQuote <- rtQuoteFuture
      dbQuote <- dbQuoteFuture
    } yield rtQuote.map(q => dbQuote.getOrElse(BS()) ++ q) ?? dbQuote
  }

  def findOne[T](symbol: String)(fields: String*)(implicit r: BSONDocumentReader[T]): Future[Option[T]] = {
    mcQBS.find(BS("symbol" -> symbol), fields.toBsonFields).one[T]
  }

  def findQuotes[T](symbols: Seq[String])(fields: String*)(implicit r: BSONDocumentReader[T]): Future[Seq[T]] = {
    mcQBS.find(BS("symbol" -> BS("$in" -> symbols)), fields.toBsonFields).cursor[T]().collect[Seq]()
  }

  def getSymbolsForCsvUpdate(implicit ec: ExecutionContext): Cursor[BS] = {
    mcQBS.find(BS("active" -> true, "$or" -> BSONArray(Seq(
      BS("yfDynLastUpdated" -> BS("$exists" -> false)),
      BS("yfDynLastUpdated" -> BS("$lte" -> new DateTime().minusMinutes(15)))
    ))), BS("symbol" -> 1)).cursor[BS]()
  }

  def getSymbolsForKeyStatisticsUpdate(implicit ec: ExecutionContext): Cursor[BS] = {
    mcQBS.find(BS("active" -> true, "$or" -> BSONArray(Seq(
      BS("yfKeyStatsLastUpdated" -> BS("$exists" -> false)),
      BS("yfKeyStatsLastUpdated" -> BS("$lte" -> new DateTime().minusDays(2)))
    ))), BS("symbol" -> 1)).cursor[BS]()
  }

  def updateQuote(symbol: String, doc: BS) = mcQBS.update(BS("symbol" -> symbol), BS("$set" -> doc))

}
