package com.shocktrade.client.discover

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.dialogs.StockQuoteDialog
import com.shocktrade.client.dialogs.StockQuoteDialog.{StockQuoteDialogSupport, StockQuoteDialogSupportScope}
import com.shocktrade.client.discover.ResearchController._
import com.shocktrade.client.users.{PersonalSymbolSupport, PersonalSymbolSupportScope}
import com.shocktrade.client.{GameStateService, GlobalLoading}
import com.shocktrade.common.SecuritiesHelper._
import com.shocktrade.common.events.RemoteEvent.StockUpdateEvent
import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.ResearchQuote
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Research Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class ResearchController($scope: ResearchScope, $rootScope: Scope, $cookies: Cookies, $timeout: Timeout, toaster: Toaster,
                              @injected("GameStateService") gameStateService: GameStateService,
                              @injected("ResearchService") researchService: ResearchService,
                              @injected("StockQuoteDialog") stockQuoteDialog: StockQuoteDialog)
  extends Controller with GlobalLoading with PersonalSymbolSupport with StockQuoteDialogSupport {

  // search reference data components
  private var exchangeCounts = js.Dictionary[Int]()
  $scope.filteredResults = emptyArray
  $scope.searchResults = emptyArray

  //////////////////////////////////////////////////////////////////////
  //              Public Data
  //////////////////////////////////////////////////////////////////////

  $scope.exchangeSets = js.Dictionary(
    "AMEX" -> true,
    "NASDAQ" -> true,
    "NYSE" -> true,
    "OTCBB" -> true,
    "OTHER_OTC" -> true
  )

  $scope.maxResultsSet = MaxResultsSet
  $scope.priceRanges = PriceRanges
  $scope.volumeRanges = VolumeRanges
  $scope.percentages = Percentages
  $scope.changePercentages = ChangePercentages
  $scope.searchOptions = new ResearchOptions(maxResults = MaxResultsSet(1))

  //////////////////////////////////////////////////////////////////////
  //              Public Functions
  //////////////////////////////////////////////////////////////////////

  $scope.getExchangeCount = (aExchange: js.UndefOr[String]) => {
    (aExchange.map(e => exchangeCounts.getOrElse(e, 0)) getOrElse 0).toDouble
  }

  $scope.getExchangeSet = (aExchange: js.UndefOr[String]) => {
    aExchange.map(e => exchangeSets.getOrElse(e, false)) getOrElse false
  }

  $scope.filterExchanges = () => syncLoading($scope, $timeout) {
    $scope.searchResults filter { q =>
      val exchange = normalizeExchange(q.exchange)
      exchangeSets.getOrElse(exchange, false)
    }
  }

  $scope.getSearchResultClass = (aCount: js.UndefOr[Int]) => getSearchResultClass(aCount)

  $scope.getSearchResultsCount = () => $scope.filteredResults.length

  $scope.columnAlign = (aColumn: js.UndefOr[String]) => aColumn map columnAlign

  $scope.rowClass = (column: js.UndefOr[String], row: js.UndefOr[ResearchQuote]) => {
    if (column.contains("symbol")) row.flatMap(_.exchange) else column
  }

  $scope.research = (aSearchOptions: js.UndefOr[ResearchOptions]) => aSearchOptions map research

  private def research(searchOptions: ResearchOptions): js.Promise[HttpResponse[js.Array[ResearchQuote]]] = {
    $scope.filteredResults = emptyArray
    $scope.searchResults = emptyArray

    // execute the search
    val outcome = researchService.research(searchOptions)
    asyncLoading($scope)(outcome) onComplete {
      case Success(response) =>
        val results = response.data
        val exchanges = js.Dictionary[Int]()
        results.map(q => q.copy(market = q.exchange, exchange = normalizeExchange(q.exchange))).foreach { q =>
          val exchange = q.exchange.orNull

          // count the quotes by exchange
          if (!exchanges.contains(exchange)) exchanges(exchange) = 1 else exchanges(exchange) = exchanges(exchange) + 1

          // add missing exchanges to our set
          if (!exchangeSets.contains(exchange)) exchangeSets(exchange) = true
        }

        // update the exchange counts
        $scope.$apply { () =>
          exchangeCounts = exchanges
          $scope.searchResults = results
          $scope.filteredResults = results
          $scope.filterExchanges()
        }

        // save the search options
        $cookies.putObject(CookieName, searchOptions)

      case Failure(e) =>
        console.error(s"Quote Search Failed - json => ${angular.toJson(searchOptions, pretty = false)}")
        toaster.error("Failed to execute search")
    }
    outcome
  }

  //////////////////////////////////////////////////////////////////////
  //              Private Functions
  //////////////////////////////////////////////////////////////////////

  private def exchangeSets: js.Dictionary[Boolean] = $scope.exchangeSets

  private def getSearchResultClass(count: js.UndefOr[Int]): String = {
    count.map {
      case n if n < 250 => "results_small"
      case n if n < 350 => "results_medium"
      case _ => "results_large"
    } getOrElse "results_none"
  }

  private def columnAlign(column: String): String = if (column == "symbol") "left" else "right"

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // retrieve the search options cookie
  $cookies.getObject[ResearchOptions](CookieName) foreach { options =>
    if (isDefined(options)) {
      console.log(s"Retrieved search options from cookie '$CookieName': ${angular.toJson(options, pretty = false)}")
      $scope.searchOptions = options
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Events
  ///////////////////////////////////////////////////////////////////////////

  $rootScope.onStockUpdateEvent { (_, stockUpdateEvent) =>
    val updatedSearchResults = getUpdatedSearchResults(stockUpdateEvent)
    $scope.$apply(() => $scope.filteredResults = updatedSearchResults)
  }

  private def getUpdatedSearchResults(stockUpdateEvent: StockUpdateEvent): js.Array[ResearchQuote] = {
    val tickers = stockUpdateEvent.tickers
    console.info(s"Updating ${tickers.length} stocks...")
    val tickerMap = js.Dictionary((for {ticker <- tickers; symbol <- ticker.symbol.toList} yield (symbol, ticker)): _*)

    $scope.filteredResults map { searchResult =>
      searchResult.symbol.toOption.flatMap(tickerMap.get) map { t =>
        searchResult.copy(lastTrade = t.lastTrade)
      } getOrElse searchResult
    }
  }

}

/**
 * Research Controller Singleton
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ResearchController {
  // data collections
  private val MaxResultsSet: js.Array[Int] = js.Array(10, 25, 50, 75, 100, 150, 200, 250)
  private val PriceRanges: js.Array[Int] = js.Array(0, 1, 2, 5, 10, 15, 20, 25, 30, 40, 50, 75, 100)
  private val VolumeRanges: js.Array[Int] = js.Array(0, 1000, 5000, 10000, 20000, 50000, 100000, 250000, 500000, 1000000, 5000000, 10000000, 20000000, 50000000)
  private val Percentages: js.Array[Int] = js.Array(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100)
  private val ChangePercentages: js.Array[Int] = js.Array(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, -5, -10, -15, -25, -50, -75, -100)
  private val CookieName = "ShockTrade_Research_SearchOptions"

  /**
   * Research Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait ResearchScope extends Scope with PersonalSymbolSupportScope with StockQuoteDialogSupportScope {
    // variables
    var exchangeSets: js.Dictionary[Boolean] = js.native
    var maxResultsSet: js.Array[Int] = js.native
    var priceRanges: js.Array[Int] = js.native
    var volumeRanges: js.Array[Int] = js.native
    var percentages: js.Array[Int] = js.native
    var changePercentages: js.Array[Int] = js.native
    var searchOptions: ResearchOptions = js.native

    // search results
    var filteredResults: js.Array[ResearchQuote] = js.native
    var searchResults: js.Array[ResearchQuote] = js.native

    // functions
    var getExchangeCount: js.Function1[js.UndefOr[String], Double] = js.native
    var getExchangeSet: js.Function1[js.UndefOr[String], Boolean] = js.native
    var filterExchanges: js.Function0[js.Array[ResearchQuote]] = js.native
    var getSearchResultClass: js.Function1[js.UndefOr[Int], js.UndefOr[String]] = js.native
    var getSearchResultsCount: js.Function0[Int] = js.native
    var columnAlign: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native
    var rowClass: js.Function2[js.UndefOr[String], js.UndefOr[ResearchQuote], js.UndefOr[String]] = js.native
    var research: js.Function1[js.UndefOr[ResearchOptions], js.UndefOr[js.Promise[HttpResponse[js.Array[ResearchQuote]]]]] = js.native

  }

}