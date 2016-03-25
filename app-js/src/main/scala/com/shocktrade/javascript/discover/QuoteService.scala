package com.shocktrade.javascript.discover

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.shocktrade.javascript.models.{BSONObjectID, HistoricalQuote, OrderQuote}

import scala.scalajs.js

/**
  * Quote Services
  * @author lawrence.daniels@gmail.com
  */
class QuoteService($http: Http) extends Service {

  def autoCompleteSymbols(searchTerm: String, maxResults: Int) = {
    val queryString = params("searchTerm" -> searchTerm, "maxResults" -> maxResults)
    $http.get[js.Array[AutoCompletedQuote]](s"/api/quotes/autoComplete$queryString")
  }

  def getExchangeCounts = $http.get[js.Array[js.Dynamic]]("/api/exchanges")

  def getFilterQuotes(filter: js.Dynamic) = {
    required("filter", filter)
    $http.post[js.Dynamic]("/api/quotes/filter/mini", filter)
  }

  def getPricing(symbols: js.Array[String]) = {
    $http.post[js.Dynamic]("/api/quotes/pricing", symbols)
  }

  def getStockQuoteList(symbols: js.Array[String]) = {
    $http.post[js.Array[js.Dynamic]]("/api/quotes/list", symbols)
  }

  def getStockQuote(symbol: String) = {
    $http.get[OrderQuote](s"/api/quotes/symbol/$symbol")
  }

  def getTradingHistory(symbol: String) = {
    $http.get[js.Array[HistoricalQuote]](s"/api/quotes/tradingHistory/$symbol")
  }

}

/**
  * Auto-Completed Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait AutoCompletedQuote extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var symbol: js.UndefOr[String]
  var name: js.UndefOr[String]
  var exchange: js.UndefOr[String]
  var assetType: js.UndefOr[String]
  var icon: js.UndefOr[String]
}

