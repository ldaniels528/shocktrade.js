package com.shocktrade.javascript.discover

import com.greencatsoft.angularjs.core.HttpPromise.promise2future
import com.greencatsoft.angularjs.core.{HttpService, Log}
import com.greencatsoft.angularjs.{Factory, Service, injectable}
import com.shocktrade.javascript.ServiceSupport
import com.shocktrade.javascript.app.model.{BasicQuote, MarketStatus, TransactionHistory}
import prickle.Unpickle

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.JSON

/**
 * Quote Services
 * @author lawrence.daniels@gmail.com
 */
@injectable("QuoteService")
class QuoteService($http: HttpService, $log: Log) extends Service with ServiceSupport {

  def autoCompleteSymbols(searchTerm: String, maxResults: Int) = {
    $http.get(s"/api/quotes/autocomplete?searchTerm=$searchTerm&maxResults=$maxResults")
  }

  def getTradingHistory(symbol: String) = flatten {
    val future: Future[js.Any] = $http.get(s"/api/quotes/tradingHistory/$symbol")
    future
      .map(JSON.stringify(_))
      .map(Unpickle[TransactionHistory].fromString(_))
  }

  def loadStockQuote(symbol: String) = $http.get(s"/api/quotes/symbol/$symbol").mapTo[js.Dynamic]

  def loadStockQuoteList(symbols: js.Array[String]) = flatten {
    val future: Future[js.Any] = $http.post("/api/quotes/list", symbols)
    future
      .map(JSON.stringify(_))
      .map(Unpickle[BasicQuote].fromString(_))
  }

  //def getFilterQuotes(filter: js.Any): HttpPromise = ???

  def getMarketStatus = flatten {
    // {"stateChanged":false,"active":false,"sysTime":1392092448795,"delay":-49848795,"start":1392042600000,"end":1392066000000}
    val future: Future[js.Any] = $http.get("/api/tradingClock/status/0")
    future
      .map(JSON.stringify(_))
      .map(Unpickle[MarketStatus].fromString(_))
  }

  def getRiskLevel(symbol: String) = $http.get(s"/api/quotes/riskLevel/$symbol").mapTo[String]

}

@injectable("QuoteService")
class QuoteServiceFactory(http: HttpService, log: Log) extends Factory[QuoteService] {

  override def apply(): QuoteService = new QuoteService(http, log)

}