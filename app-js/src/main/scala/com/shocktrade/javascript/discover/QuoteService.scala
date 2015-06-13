package com.shocktrade.javascript.discover

import biz.enef.angulate.core.{HttpPromise, HttpService}
import biz.enef.angulate.{Service, named}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.annotation.JSExportAll

/**
 * Quote Services
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class QuoteService($rootScope: js.Dynamic, $http: HttpService, @named("MySession") mySession: MySession) extends Service {
  g.console.log("QuoteService is loading...")

  def autoCompleteSymbols: js.Function = (searchTerm: String, maxResults: Int) => {
    val queryString = params("searchTerm" -> searchTerm, "maxResults" -> maxResults)
    $http.get[js.Dynamic](s"/api/quotes/autocomplete$queryString")
  }

  def getFilterQuotes: js.Function = (filter: js.Dynamic) => {
    mySession.userProfile.OID_? map { userID =>
      $http.post(s"/api/profile/$userID/quotes/filter/mini", filter)
    } getOrElse {
      $http.post("/api/quotes/filter/mini", filter)
    }
  }

  def getPricing: js.Function = (symbols: js.Array[String]) => $http.post[js.Dynamic]("/api/quotes/pricing", symbols)

  def getRiskLevel: js.Function = (symbol: String) => $http.get[js.Dynamic](s"/api/quotes/riskLevel/$symbol")

  def getStockQuoteList: js.Function = (symbols: js.Array[String]) => $http.post[js.Dynamic]("/api/quotes/list", symbols)

  def getStockQuote: js.Function = (symbol: String) => $http.get[js.Dynamic](s"/api/quotes/symbol/$symbol")

  def getTradingHistory: js.Function = (symbol: String) => getTradingHistory_@(symbol)

  def getTradingHistory_@(symbol: String) = $http.get[js.Array[js.Dynamic]](s"/api/quotes/tradingHistory/$symbol")

  ////////////////////////////////////////////////////////////////////
  //			Exchange Functions
  ///////////////////////////////////////////////////////////////////

  def getExchangeCounts: js.Function = () => $http.get[js.Dynamic]("/api/exchanges")

  def setExchangeState: js.Function = (id: String, exchange: String, state: Boolean) => {
    if (state)
      $http.put[js.Dynamic](s"/api/profile/$id/exchange/$exchange")
    else
      $http.delete[js.Dynamic](s"/api/profile/$id/exchange/$exchange")
  }

  ////////////////////////////////////////////////////////////////////
  //			Sector Exploration Functions
  ///////////////////////////////////////////////////////////////////

  def loadSectorInfo: js.Function1[String, HttpPromise[js.Array[js.Dynamic]]] = (symbol: String) => {
    $http.get[js.Array[js.Dynamic]](s"/api/explore/symbol/$symbol")
  }

  def loadSectors: js.Function0[HttpPromise[js.Array[js.Dynamic]]] = () => {
    mySession.userProfile.OID_? map { userID =>
      $http.get[js.Array[js.Dynamic]](s"/api/profile/$userID/explore/sectors")
    } getOrElse {
      $http.get[js.Array[js.Dynamic]]("/api/explore/sectors")
    }
  }

  def loadNAICSSectors: js.Function0[HttpPromise[js.Array[js.Dynamic]]] = () => {
    mySession.userProfile.OID_? map { userID =>
      $http.get[js.Array[js.Dynamic]](s"/api/profile/$userID/explore/naics/sectors")
    } getOrElse {
      $http.get[js.Array[js.Dynamic]]("/api/explore/naics/sectors")
    }
  }

  def loadIndustries: js.Function1[String, HttpPromise[js.Array[js.Dynamic]]] = (sector: String) => {
    val queryString = params("sector" -> sector)
    mySession.userProfile.OID_? map { userID =>
      $http.get[js.Array[js.Dynamic]](s"/api/profile/$userID/explore/industries$queryString")
    } getOrElse {
      $http.get[js.Array[js.Dynamic]](s"/api/explore/industries$queryString")
    }
  }

  def loadSubIndustries: js.Function2[String, String, HttpPromise[js.Array[js.Dynamic]]] = (sector: String, industry: String) => {
    val queryString = params("sector" -> sector, "industry" -> industry)
    mySession.userProfile.OID_? map { userID =>
      $http.get[js.Array[js.Dynamic]](s"/api/profile/$userID/explore/subIndustries$queryString")
    } getOrElse {
      $http.get[js.Array[js.Dynamic]](s"/api/explore/subIndustries$queryString")
    }
  }

  def loadIndustryQuotes: js.Function3[String, String, String, HttpPromise[js.Array[js.Dynamic]]] = (sector: String, industry: String, subIndustry: String) => {
    val queryString = params("sector" -> sector, "industry" -> industry, "subIndustry" -> subIndustry)
    mySession.userProfile.OID_? map { userID =>
      $http.get[js.Array[js.Dynamic]](s"/api/profile/$userID/explore/quotes$queryString")
    } getOrElse {
      $http.get[js.Array[js.Dynamic]](s"/api/explore/quotes$queryString")
    }
  }

  private def setFavorites(updatedQuotes: js.Array[js.Dynamic]) = {
    updatedQuotes.foreach { quote =>
      quote.favorite = $rootScope.FavoriteSymbols.isFavorite(quote.symbol)
    }
    updatedQuotes
  }

}
