package com.shocktrade.client.directives

import com.shocktrade.client.QuoteCache
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.Directive.{ElementRestriction, LinkSupport, TemplateSupport}
import io.scalajs.npm.angularjs._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Stock Quote Directive
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 * @example {{{ <stock-quote symbol="{{ quote.symbol }}" field="lastTrade"></stock-quote> }}}
 */
class StockQuoteDirective(@injected("QuoteCache") cache: QuoteCache) extends Directive
  with ElementRestriction
  with LinkSupport[StockQuoteDirectiveScope]
  with TemplateSupport {

  override val scope = new StockQuoteDirectiveInputs(symbol = "@symbol", field = "@field")
  override val template = """{{ value }}"""

  override def link(scope: StockQuoteDirectiveScope, element: JQLite, attrs: Attributes): Unit = {
    scope.$watch("symbol", (aSymbol: js.UndefOr[String]) => aSymbol foreach (getOrLoadStockQuote(scope, _)))
  }

  def getOrLoadStockQuote(scope: StockQuoteDirectiveScope, symbol: String) = {
    cache.get(symbol) onComplete {
      case Success(quote) =>
        val props = quote.asInstanceOf[js.Dictionary[js.Any]]
        scope.$apply(() => scope.value = props.get(scope.field).orUndefined)
      case Failure(e) =>
        if (scope.failed.isEmpty) {
          console.error(s"Error retrieving quote for $symbol: ${e.displayMessage}")
          scope.failed = true
        }
    }
  }

}

/**
 * Stock Quote Directive Input Parameters
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class StockQuoteDirectiveInputs(val symbol: String, val field: String) extends js.Object

/**
 * Stock Quote Directive Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait StockQuoteDirectiveScope extends StockQuoteDirectiveInputs with Scope {
  var failed: js.UndefOr[Boolean] = js.native
  var value: js.UndefOr[js.Any] = js.native
}