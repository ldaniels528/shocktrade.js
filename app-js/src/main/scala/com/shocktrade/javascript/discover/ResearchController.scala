package com.shocktrade.javascript.discover

import biz.enef.angulate._
import biz.enef.angulate.core.{HttpService, Timeout}
import com.ldaniels528.angularjs.{CookieStore, Toaster}
import com.shocktrade.javascript.MainController
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.discover.ResearchController._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Research Controller
 * @author lawrence.daniels@gmail.com
 */
class ResearchController($scope: js.Dynamic, $cookieStore: CookieStore, $http: HttpService, $timeout: Timeout, toaster: Toaster)
  extends ScopeController {

  // search reference data components
  private var exchangeCounts = js.Dictionary[Int]()
  private var filteredResults = emptyArray[js.Dynamic]
  private var searchResults = emptyArray[js.Dynamic]

  //////////////////////////////////////////////////////////////////////
  //              Public Data
  //////////////////////////////////////////////////////////////////////

  $scope.exchangeSets = js.Dictionary[Boolean](
    "AMEX" -> true,
    "NASDAQ" -> true,
    "NYSE" -> true,
    "OTCBB" -> true,
    "OTHER_OTC" -> true
  )

  $scope.maxResultsSet = maxResultsSet

  $scope.priceRanges = priceRanges

  $scope.volumeRanges = volumeRanges

  $scope.percentages = percentages

  $scope.changePercentages = changePercentages

  //////////////////////////////////////////////////////////////////////
  //              Public Functions
  //////////////////////////////////////////////////////////////////////

  $scope.getFilteredResults = () => filteredResults

  $scope.getSearchResults = () => searchResults

  $scope.getExchangeCount = (exchange: js.UndefOr[String]) => getExchangeCount(exchange)

  $scope.searchOptions = JS(
    sortBy = null,
    reverse = false,
    maxResults = maxResultsSet(1)
  )

  $scope.getExchangeSet = (exchange: js.UndefOr[String]) => getExchangeSet(exchange)

  $scope.filterExchanges = () => filterExchanges()

  $scope.getSearchResults = () => filteredResults

  $scope.getSearchResultClass = (count: js.UndefOr[Int]) => getSearchResultClass(count)

  $scope.getSearchResultsCount = () => filteredResults.length

  $scope.columnAlign = (column: String) => columnAlign(column)

  $scope.rowClass = (column: String, row: js.Dynamic) => rowClass(column, row)

  $scope.quoteSearch = (searchOptions: js.Dynamic) => quoteSearch(searchOptions)

  //////////////////////////////////////////////////////////////////////
  //              Private Functions
  //////////////////////////////////////////////////////////////////////

  private def exchangeSets = $scope.exchangeSets.as[js.Dictionary[Boolean]]

  private def filterExchanges() {
    startLoading()
    filteredResults = searchResults.filter { q =>
      val exchange = MainController.normalizeExchange(q.exchange.as[String])
      exchangeSets.getOrElse(exchange, false)
    }
    $timeout(() => stopLoading(), 500)
  }

  private def getExchangeSet(exchange: js.UndefOr[String]): Boolean = exchange.map(e => exchangeSets.getOrElse(e, false)) getOrElse false

  private def getExchangeCount(exchange: js.UndefOr[String]): Double = (exchange.map(e => exchangeCounts.getOrElse(e, 0)) getOrElse 0).toDouble

  private def getSearchResultClass(count: js.UndefOr[Int]) = {
    count.map {
      case n if n < 250 => "results_small"
      case n if n < 350 => "results_medium"
      case _ => "results_large"
    } getOrElse "results_none"
  }

  private def columnAlign(column: String) = if (column == "symbol") "left" else "right"

  private def quoteSearch(searchOptions: js.Dynamic) = {
    filteredResults = emptyArray
    searchResults = emptyArray

    // execute the search
    startLoading()
    g.console.log(s"searchOptions = ${angular.toJson(searchOptions, pretty = false)}")
    $http.post[js.Array[js.Dynamic]]("/api/research/search", searchOptions) onComplete {
      case Success(results) =>
        val exchanges = js.Dictionary[Int]()
        results.foreach { q =>
          // normalize the exchange
          val exchange = MainController.normalizeExchange(q.exchange.as[String])
          q.market = q.exchange
          q.exchange = exchange

          // count the quotes by exchange
          if (!exchanges.contains(exchange)) exchanges(exchange) = 1 else exchanges(exchange) = exchanges(exchange) + 1

          // add missing exchanges to our set
          if (!exchangeSets.contains(exchange)) {
            exchangeSets(exchange) = true
          }
        }

        // update the exchange counts
        exchangeCounts = exchanges
        searchResults = results
        $scope.filterExchanges()
        stopLoading()

        // save the search options
        $cookieStore.put(cookieName, searchOptions)

      case Failure(e) =>
        g.console.error(s"Quote Search Failed - json => ${angular.toJson(searchOptions, pretty = false)}")
        toaster.error("Failed to execute search")
        stopLoading()
    }
  }

  private def rowClass(column: String, row: js.Dynamic) = if (column == "symbol") row.exchange else column

  private def startLoading() {
    $scope.loading = true
    $scope.startLoading()
  }

  private def stopLoading() {
    $scope.loading = false
    $scope.stopLoading()
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // retrieve the search options cookie
  $cookieStore.get[js.Dynamic](cookieName) foreach { options =>
    if (isDefined(options)) {
      g.console.log(s"Retrieved search options from cookie '$cookieName': ${angular.toJson(options, pretty = false)}")
      $scope.searchOptions = options
    }
  }

}

/**
 * Research Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object ResearchController {
  private val cookieName = "ShockTrade_Research_SearchOptions"

  // data collections
  private val maxResultsSet = js.Array(10, 25, 50, 75, 100, 150, 200, 250)
  private val priceRanges = js.Array(0, 1, 2, 5, 10, 15, 20, 25, 30, 40, 50, 75, 100)
  private val volumeRanges = js.Array(0, 1000, 5000, 10000, 20000, 50000, 100000, 250000, 500000, 1000000, 5000000, 10000000, 20000000, 50000000)
  private val percentages = js.Array(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100)
  private val changePercentages = js.Array(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, -5, -10, -15, -25, -50, -75, -100)

}
