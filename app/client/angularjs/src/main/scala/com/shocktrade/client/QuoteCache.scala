package com.shocktrade.client

import com.shocktrade.client.discover.QuoteService
import com.shocktrade.common.models.quote.CompleteQuote
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.util.DurationHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Quote Cache Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class QuoteCache($timeout: Timeout, @injected("QuoteService") quoteService: QuoteService) extends Service {
  private val quotes = js.Dictionary[Future[CompleteQuote]]()

  /**
    * Synchronously retrieves the quote for the given symbol
    * @param symbol the given symbol
    * @return the quote or <tt>undefined</tt> if it is not immediately available
    */
  def apply(symbol: String)(implicit ec: ExecutionContext): js.UndefOr[CompleteQuote] = {
    val value = quotes.get(symbol) flatMap {
      case f if f.isCompleted => f.value.flatMap(_.toOption)
      case f => None
    } orUndefined

    if (value.isEmpty) get(symbol)
    value
  }

  /**
    * Asynchronously retrieves the quote for the given symbol
    * @param symbol the given symbol
    * @return the promise of a quote
    */
  def get(symbol: String)(implicit ec: ExecutionContext): Future[CompleteQuote] = {
    quotes.getOrElseUpdate(symbol, loadQuote(symbol))
  }

  /**
    * Asynchronously retrieves the quotes for the given symbols
    * @param symbols the given array of symbols
    * @return the promise of a collection of quotes
    */
  def get(symbols: js.Array[String])(implicit ec: ExecutionContext): Future[Seq[CompleteQuote]] = {
    Future.sequence(symbols.toSeq map { symbol =>
      quotes.getOrElseUpdate(symbol, loadQuote(symbol))
    })
  }

  /**
    * Retrieves a quote for the given symbol (e.g. "AAPL")
    * @param symbol the given symbol
    * @param ec     the given [[ExecutionContext execution context]]
    * @return the promise of a quote
    */
  private def loadQuote(symbol: String)(implicit ec: ExecutionContext) = {
    val response = quoteService.getCompleteQuote(symbol).map(_.data)
    response onComplete {
      case Success(_) =>
      case Failure(e) =>
        console.log(s"Failed to load quote for symbol: $symbol - ${e.displayMessage}")
        $timeout(() => quotes.remove(symbol), 5.seconds)
    }
    response
  }

}
