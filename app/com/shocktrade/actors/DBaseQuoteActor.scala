package com.shocktrade.actors

import akka.actor.{Actor, ActorLogging}
import com.shocktrade.actors.QuoteMessages._
import com.shocktrade.controllers.QuoteFiltering
import com.shocktrade.controllers.QuoteResources._
import com.shocktrade.models.profile.Filter
import com.shocktrade.models.quote.QuoteFilter
import play.api.libs.json.Json.{obj => JS, _}
import play.api.libs.json.{Json, JsArray, JsObject}
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument => BS}
import reactivemongo.core.commands.LastError

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * Database Stock Quote Actor
 * @author lawrence.daniels@gmail.com
 */
class DBaseQuoteActor() extends Actor with ActorLogging with QuoteFiltering {
  private lazy val mcJS = db.collection[JSONCollection]("Stocks")
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

  private def findQuotes(filter: QuoteFilter)(implicit ec: ExecutionContext): Future[Seq[JsObject]] = {
    val query = filter.makeQuery
    log.info(s"findQuotes: query = $query}")
    mcJS.find(query, JS()).cursor[JsObject].collect[Seq]()
  }

  /**
   * Loads a quote from disk for the given symbol
   * @param symbol the given symbol
   * @param ec the given [[ExecutionContext]]
   * @return the promise of an option of a quote loaded from disk
   */
  private def loadQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[JsObject]] = {
    // db.Stocks.find({$or:[{symbol:"GFOOE"}, {"changes.symbol":{$in:["GFOOE"]}}]})
    mcJS.find(JS("$or" -> JsArray(Seq(JS("symbol" -> symbol), JS("changes.symbol" -> JS("$in" -> Seq(symbol)))))), limitFields)
      .cursor[JsObject]
      .collect[Seq](1) map (_.headOption)
  }

  /**
   * Loads a quote from disk for the given symbol
   * @param symbol the given symbol
   * @param ec the given [[ExecutionContext]]
   * @return the promise of an option of a quote loaded from disk
   */
  private def loadFullQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[JsObject]] = {
    // db.Stocks.find({$or:[{symbol:"GFOOE"}, {"changes.symbol":{$in:["GFOOE"]}}]})
    mcJS.find(JS("$or" -> JsArray(Seq(JS("symbol" -> symbol), JS("changes.symbol" -> JS("$in" -> Seq(symbol)))))))
      .cursor[JsObject]
      .collect[Seq](1) map (_.headOption)
  }

  private def loadQuotes(symbols: Seq[String])(implicit ec: ExecutionContext): Future[JsArray] = {
    mcJS.find(JS("symbol" -> JS("$in" -> symbols)), limitFields).cursor[JsObject].collect[Seq]() map JsArray
  }

  /**
   * Saves a quote to disk
   * @param symbol the quote's ticker symbol
   * @param quote the given quote to persist
   * @return the promise of the [[LastError last error]]
   */
  private def saveQuote(symbol: String, quote: JsObject): Future[LastError] = {
    log.info(s"Saving quote for $symbol...")
    mcJS.update(selector = JS("symbol" -> symbol), update = JS("$set" -> quote), upsert = true)
  }

}
