package com.shocktrade.javascript.discover

import com.shocktrade.javascript.models.HistoricalQuote
import com.shocktrade.javascript.models.contest.OrderQuote
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.{Http, HttpConfig}

import scala.scalajs.js

/**
  * Quote Services
  * @author lawrence.daniels@gmail.com
  */
class QuoteService($http: Http) extends Service {

  def autoCompleteSymbols(searchTerm: String, maxResults: Int) = {
    $http[js.Array[AutoCompletedQuote]](HttpConfig(
      method = "GET",
      url = "/api/quotes/search",
      params = js.Dictionary("searchTerm" -> searchTerm, "maxResults" -> maxResults)
    ))
  }

  def getExchangeCounts = $http.get[js.Array[js.Dynamic]]("/api/exchanges")

  def getFilterQuotes(aFilter: js.UndefOr[js.Any]) = aFilter foreach { filter =>
    $http.post[js.Dynamic]("/api/quotes/filter/mini", filter)
  }

  def getPricing(symbols: js.Array[String]) = {
    $http.post[js.Dynamic]("/api/quotes/pricing", symbols)
  }

  def getStockQuoteList(symbols: js.Array[String]) = {
    $http.post[js.Array[BasicQuote]]("/api/quotes/list", symbols)
  }

  def getStockQuote(symbol: String) = {
    $http.get[OrderQuote](s"/api/quote/$symbol/discover")
  }

  def getTradingHistory(symbol: String) = {
    $http.get[js.Array[HistoricalQuote]](s"/api/quote/$symbol/history")
  }

}

/**
  * Auto-Completed Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait AutoCompletedQuote extends js.Object {
  var _id: js.UndefOr[String] = js.native
  var symbol: js.UndefOr[String] = js.native
  var name: js.UndefOr[String] = js.native
  var exchange: js.UndefOr[String] = js.native
  var assetType: js.UndefOr[String] = js.native
  var icon: js.UndefOr[String] = js.native
}

/**
  * Basic Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait BasicQuote extends js.Object {
  var _id: js.UndefOr[String] = js.native
  var symbol: js.UndefOr[String] = js.native
  var exchange: js.UndefOr[String] = js.native
}
