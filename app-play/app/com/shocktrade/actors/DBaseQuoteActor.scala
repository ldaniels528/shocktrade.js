package com.shocktrade.actors

import akka.actor.{Actor, ActorLogging}
import com.shocktrade.actors.QuoteMessages._
import com.shocktrade.controllers.QuotesController._
import com.shocktrade.models.quote.QuoteFilter
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONArray, BSONDocument => BS}

import scala.util.{Failure, Success}

/**
 * Database Stock Quote Actor
 * @author lawrence.daniels@gmail.com
 */
class DBaseQuoteActor() extends Actor with ActorLogging {
  private lazy val mcBS = db.collection[BSONCollection]("Stocks")

  import context.dispatcher

  override def receive = {
    case FindQuotes(filter) =>
      val mySender = sender()
      findQuotes(filter) onComplete {
        case Success(quotes) => mySender ! quotes
        case Failure(e) => mySender ! e
      }

    case GetFullQuote(symbol) =>
      val mySender = sender()
      loadFullQuote(symbol) foreach (mySender ! _)

    case GetQuote(symbol) =>
      val mySender = sender()
      loadQuote(symbol) foreach (mySender ! _)

    case GetQuotes(symbols) =>
      val mySender = sender()
      loadQuotes(symbols) foreach (mySender ! _)

    case SaveQuote(symbol, quote) =>
      saveQuote(symbol, quote)

    case message =>
      unhandled(message)
  }

  private def findQuotes(filter: QuoteFilter) = {
    val query = filter.makeQuery
    log.info(s"findQuotes: query = $query}")
    mcBS.find(query, BS()).cursor[BS]().collect[Seq]()
  }

  /**
   * Loads a quote from disk for the given symbol
   * @param symbol the given symbol
   * @return the promise of an option of a quote loaded from disk
   */
  private def loadQuote(symbol: String) = {
    // db.Stocks.find({$or:[{symbol:"GFOOE"}, {"changes.symbol":{$in:["GFOOE"]}}]})
    mcBS.find(BS("$or" -> BSONArray(Seq(BS("symbol" -> symbol), BS("changes.symbol" -> BS("$in" -> Seq(symbol)))))), limitFields).one[BS]
  }

  /**
   * Loads a quote from disk for the given symbol
   * @param symbol the given symbol
   * @return the promise of an option of a quote loaded from disk
   */
  private def loadFullQuote(symbol: String) = {
    // db.Stocks.find({$or:[{symbol:"GFOOE"}, {"changes.symbol":{$in:["GFOOE"]}}]})
    mcBS.find(BS("$or" -> BSONArray(Seq(BS("symbol" -> symbol), BS("changes.symbol" -> BS("$in" -> Seq(symbol))))))).one[BS]
  }

  private def loadQuotes(symbols: Seq[String]) = {
    mcBS.find(BS("symbol" -> BS("$in" -> symbols)), limitFields).cursor[BS]().collect[Seq]()
  }

  /**
   * Saves a quote to disk
   * @param symbol the quote's ticker symbol
   * @param quote the given quote to persist
   * @return the promise of the [[reactivemongo.api.commands.WriteResult write result]]
   */
  private def saveQuote(symbol: String, quote: BS) = {
    log.info(s"Saving quote for $symbol...")
    mcBS.update(selector = BS("symbol" -> symbol.toUpperCase.trim), update = BS("$set" -> quote), upsert = true)
  }

}
