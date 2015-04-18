package com.shocktrade.actors.quote

import akka.actor.{Actor, ActorLogging}
import com.shocktrade.actors.quote.QuoteMessages._
import com.shocktrade.controllers.QuoteResources
import QuoteResources._
import play.api.libs.json.Json.{obj => JS, _}
import play.api.libs.json.{JsArray, JsObject}
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError

import scala.concurrent.{ExecutionContext, Future}

/**
 * Database Stock Quote Actor
 * @author lawrence.daniels@gmail.com
 */
class DBaseQuoteActor() extends Actor with ActorLogging {
  private lazy val mcQ = db.collection[JSONCollection]("Stocks")

  import context.dispatcher

  override def receive = {
    case GetQuote(symbol) =>
      val mySender = sender()
      loadQuote(symbol) foreach (mySender ! _)

    case SaveQuote(symbol, quote) =>
      saveQuote(symbol, quote)

    case message =>
      unhandled(message)
  }

  /**
   * Loads a quote from disk for the given symbol
   * @param symbol the given symbol
   * @param ec the given [[ExecutionContext]]
   * @return the promise of an option of a quote loaded from disk
   */
  private def loadQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[JsObject]] = {
    // db.Stocks.find({$or:[{symbol:"GFOOE"}, {"changes.symbol":{$in:["GFOOE"]}}]})
    mcQ.find(JS("$or" -> JsArray(Seq(JS("symbol" -> symbol), JS("changes.symbol" -> JS("$in" -> Seq(symbol))))))).cursor[JsObject].collect[Seq](1) map (_.headOption)
  }

  /**
   * Saves a quote to disk
   * @param symbol the quote's ticker symbol
   * @param quote the given quote to persist
   * @return the promise of the [[LastError last error]]
   */
  private def saveQuote(symbol: String, quote: JsObject): Future[LastError] = {
    log.info(s"Saving quote for $symbol...")
    mcQ.update(selector = JS("symbol" -> symbol), update = JS("$set" -> quote), upsert = true)
  }

}
