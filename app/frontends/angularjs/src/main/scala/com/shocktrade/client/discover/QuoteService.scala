package com.shocktrade.client.discover

import com.shocktrade.client.discover.QuoteService.BasicQuote
import com.shocktrade.common.models.quote._
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Quote Services
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class QuoteService($http: Http) extends Service {

  def autoCompleteSymbols(searchTerm: String, maxResults: Int): js.Promise[HttpResponse[js.Array[AutoCompleteQuote]]] = {
    $http.get(s"/api/quotes/search?searchTerm=$searchTerm&maxResults=$maxResults")
  }

  def getBasicQuote(symbol: String): js.Promise[HttpResponse[BasicQuote]] = {
    $http.get(s"/api/quote/$symbol/basic")
  }

  def getBasicQuotes(symbols: js.Array[String]): js.Promise[HttpResponse[js.Array[OrderQuote]]] = {
    $http.post("/api/quotes/list", data = symbols)
  }

  def getCompleteQuote(symbol: String): js.Promise[HttpResponse[CompleteQuote]] = {
    $http.get(s"/api/quote/$symbol/discover")
  }

  def getExchangeCounts: js.Promise[HttpResponse[js.Array[js.Dynamic]]] = $http.get("/api/exchanges")

  def getFilterQuotes(aFilter: js.UndefOr[js.Any]): js.Promise[HttpResponse[js.Dynamic]] = {
    aFilter.flat.toOption match {
      case Some(filter) =>
        $http.post[js.Dynamic]("/api/quotes/filter/mini", filter)
      case None =>
        throw js.JavaScriptException("Filter is undefined")
    }
  }

  def getOrderQuote(symbol: String): js.Promise[HttpResponse[OrderQuote]] = {
    $http.get(s"/api/quote/$symbol/order")
  }

  def getKeyStatistics(symbol: String): js.Promise[HttpResponse[KeyStatistics]] = {
    $http.get(s"/api/quote/$symbol/statistics")
  }

  def getTradingHistory(symbol: String): js.Promise[HttpResponse[js.Array[HistoricalQuote]]] = {
    $http.get(s"/api/quote/$symbol/history")
  }

}

/**
 * Quote Services Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object QuoteService {

  type BasicQuote = js.Object

}
