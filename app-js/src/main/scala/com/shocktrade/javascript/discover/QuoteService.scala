package com.shocktrade.javascript.discover

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.core.{Http, Service}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js

/**
 * Quote Services
 * @author lawrence.daniels@gmail.com
 */
class QuoteService($http: Http, @named("MySession") mySession: MySession) extends Service {

  def autoCompleteSymbols(searchTerm: String, maxResults: Int) = {
    required("searchTerm", searchTerm)
    val queryString = params("searchTerm" -> searchTerm, "maxResults" -> maxResults)
    $http.get[js.Array[js.Dynamic]](s"/api/quotes/autocomplete$queryString")
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
    $http.get[js.Dynamic](s"/api/quotes/symbol/$symbol")
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

  private def setFavorites(updatedQuotes: js.Array[js.Dynamic]) = {
    required("updatedQuotes", updatedQuotes)
    updatedQuotes.foreach { quote =>
      quote.favorite = mySession.isFavoriteSymbol(quote.symbol.as[String])
    }
    updatedQuotes
  }

}
