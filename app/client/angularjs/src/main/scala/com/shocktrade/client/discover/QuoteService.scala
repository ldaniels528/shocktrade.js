package com.shocktrade.client.discover

import com.shocktrade.common.models.quote._
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.Http

import scala.scalajs.js

/**
  * Quote Services
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class QuoteService($http: Http) extends Service {

  def autoCompleteSymbols(searchTerm: String, maxResults: Int) = {
    $http.get[js.Array[AutoCompleteQuote]](s"/api/quotes/search?searchTerm=$searchTerm&maxResults=$maxResults")
  }

  def getBasicQuote(symbol: String) = {
    $http.get[ResearchQuote](s"/api/quote/$symbol/basic")
  }

  def getBasicQuotes(symbols: js.Array[String]) = {
    $http.post[js.Array[OrderQuote]]("/api/quotes/list", data = symbols)
  }

  def getCompleteQuote(symbol: String) = {
    $http.get[CompleteQuote](s"/api/quote/$symbol/discover")
  }

  def getExchangeCounts = {
    $http.get[js.Array[js.Dynamic]]("/api/exchanges")
  }

  def getFilterQuotes(aFilter: js.UndefOr[js.Any]) = aFilter map { filter =>
    $http.post[js.Dynamic]("/api/quotes/filter/mini", filter)
  }

  def getOrderQuote(symbol: String) = {
    $http.get[OrderQuote](s"/api/quote/$symbol/order")
  }

  def getKeyStatistics(symbol: String) = {
    $http.get[KeyStatistics](s"/api/quote/$symbol/statistics")
  }

  def getTradingHistory(symbol: String) = {
    $http.get[js.Array[HistoricalQuote]](s"/api/quote/$symbol/history")
  }

}
