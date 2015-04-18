package com.shocktrade.models

import akka.actor.Props
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.actors.quote.QuoteMessages._
import com.shocktrade.actors.quote.{DBaseQuoteActor, RealTimeQuoteActor}
import play.api.libs.json.JsObject
import play.api.libs.json.Json.{obj => JS}
import play.libs.Akka

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 * Stock Quote Manager
 * @author lawrence.daniels@gmail.com
 */
object StockQuotes {
  private val realTimeCache = TrieMap[String, JsObject]()
  private val diskCache = TrieMap[String, JsObject]()
  private val system = Akka.system
  private val quoteActor = system.actorOf(Props[RealTimeQuoteActor].withRouter(RoundRobinPool(nrOfInstances = 50)))
  private val mongoReader = system.actorOf(Props[DBaseQuoteActor].withRouter(RoundRobinPool(nrOfInstances = 50)))
  private val mongoWriter = system.actorOf(Props[DBaseQuoteActor])

  /**
   * Retrieves a real-time quote for the given symbol
   * @param symbol the given symbol (e.g. 'AAPL')
   * @param ec the given [[ExecutionContext]]
   * @return a promise of an option of a [[JsObject quote]]
   */
  def findRealTimeQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[JsObject]] = {
    realTimeCache.get(symbol) match {
      case quote@Some(q) => Future.successful(quote)
      case None =>
        implicit val timeout: Timeout = 20.seconds
        val rtQuoteFuture = (quoteActor ? GetQuote(symbol)).mapTo[Option[JsObject]]
        rtQuoteFuture.foreach(_ foreach { quote =>
          realTimeCache(symbol) = quote
          mongoWriter ! SaveQuote(symbol, quote)
        })
        rtQuoteFuture
    }
  }

  /**
   * Retrieves a database quote for the given symbol
   * @param symbol the given symbol (e.g. 'AAPL')
   * @param ec the given [[ExecutionContext]]
   * @return a promise of an option of a [[JsObject quote]]
   */
  def findDBaseQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[JsObject]] = {
    diskCache.get(symbol) match {
      case quote@Some(q) => Future.successful(quote)
      case None =>
        implicit val timeout: Timeout = 20.seconds
        val dbQuoteFuture = (mongoReader ? GetQuote(symbol)).mapTo[Option[JsObject]]
        dbQuoteFuture.foreach(_ foreach (diskCache(symbol) = _))
        dbQuoteFuture
    }
  }

  /**
   * Retrieves a complete quote; the composition of real-time quote and a disc-based quote
   * @param symbol the given ticker symbol
   * @param ec the given [[ExecutionContext]]
   * @return the [[Future promise]] of an option of a [[JsObject quote]]
   */
  def findFullQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[JsObject]] = {
    val rtQuoteFuture = findRealTimeQuote(symbol)
    val dbQuoteFuture = findDBaseQuote(symbol)
    val tupleFuture = for {rtQuote <- rtQuoteFuture; dbQuote <- dbQuoteFuture} yield (rtQuote, dbQuote)

    // return the combined quote
    tupleFuture map { case (rtQuote, dbQuote) =>
      if (rtQuote.isDefined) rtQuote.map(q => dbQuote.getOrElse(JS()) ++ q) else dbQuote
    }
  }

}
