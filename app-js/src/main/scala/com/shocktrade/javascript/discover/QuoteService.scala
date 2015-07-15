package com.shocktrade.javascript.discover

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Http
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.models.OrderQuote

import scala.scalajs.js

/**
 * Quote Services
 * @author lawrence.daniels@gmail.com
 */
class QuoteService($http: Http) extends Service {

  def autoCompleteSymbols(searchTerm: String, maxResults: Int) = {
    required("searchTerm", searchTerm)
    val queryString = params("searchTerm" -> searchTerm, "maxResults" -> maxResults)
    $http.get[js.Array[AutoCompletedQuote]](s"/api/quotes/autoComplete$queryString")
  }

  def getExchangeCounts = $http.get[js.Array[js.Dynamic]]("/api/exchanges")

  def getFilterQuotes(filter: js.Dynamic) = {
    required("filter", filter)
    $http.post[js.Dynamic]("/api/quotes/filter/mini", filter)
  }

  def getPricing(symbols: js.Array[String]) = {
    required("symbols", symbols)
    $http.post[js.Dynamic]("/api/quotes/pricing", symbols)
  }

  def getRiskLevel(symbol: String) = {
    required("symbol", symbol)
    $http.get[js.Dynamic](s"/api/quotes/riskLevel/$symbol")
  }

  def getStockQuoteList(symbols: js.Array[String]) = {
    required("symbols", symbols)
    $http.post[js.Array[js.Dynamic]]("/api/quotes/list", symbols)
  }

  def getStockQuote(symbol: String) = {
    required("symbol", symbol)
    $http.get[OrderQuote](s"/api/quotes/symbol/$symbol")
  }

  def getTradingHistory(symbol: String) = {
    required("symbol", symbol)
    $http.get[js.Array[js.Dynamic]](s"/api/quotes/tradingHistory/$symbol")
  }

  ////////////////////////////////////////////////////////////////////
  //			Sector Exploration Functions
  ///////////////////////////////////////////////////////////////////

  def loadSectorInfo(symbol: String) = {
    required("symbol", symbol)
    $http.get[js.Array[js.Dynamic]](s"/api/explore/symbol/$symbol")
  }

  def loadSectors() = $http.get[js.Array[js.Dynamic]]("/api/explore/sectors")

  def loadNAICSSectors() = $http.get[js.Array[js.Dynamic]]("/api/explore/naics/sectors")

  def loadIndustries(sector: String) = {
    required("sector", sector)
    val queryString = params("sector" -> sector)
    $http.get[js.Array[js.Dynamic]](s"/api/explore/industries$queryString")
  }

  def loadSubIndustries(sector: String, industry: String) = {
    required("sector", sector)
    required("industry", industry)
    val queryString = params("sector" -> sector, "industry" -> industry)
    $http.get[js.Array[js.Dynamic]](s"/api/explore/subIndustries$queryString")
  }

  def loadIndustryQuotes(sector: String, industry: String, subIndustry: String) = {
    required("sector", sector)
    required("industry", industry)
    required("subIndustry", subIndustry)
    val queryString = params("sector" -> sector, "industry" -> industry, "subIndustry" -> subIndustry)
    $http.get[js.Array[js.Dynamic]](s"/api/explore/quotes$queryString")
  }

}

/**
 * Auto-Completed Quote
 */
trait AutoCompletedQuote extends js.Object {
  var _id: js.Dynamic = js.native
  var symbol: js.UndefOr[String] = js.native
  var name: js.UndefOr[String] = js.native
  var exchange: js.UndefOr[String] = js.native
  var assetType: js.UndefOr[String] = js.native
  var icon: js.UndefOr[String] = js.native
}