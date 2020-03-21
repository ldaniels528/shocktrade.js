package com.shocktrade.client.discover

import com.shocktrade.client.discover.ResearchController._
import com.shocktrade.client.{GlobalLoading, MainController, RootScope}
import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.ResearchQuote
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.cookies.Cookies
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
class ResearchController($scope: ResearchScope, $cookies: Cookies, $timeout: Timeout, toaster: Toaster,
                         @injected("ResearchService") researchService: ResearchService)
  extends Controller with GlobalLoading {

  // search reference data components
  private var exchangeCounts = js.Dictionary[Int]()
  private var filteredResults: js.Array[ResearchQuote] = emptyArray
  private var searchResults: js.Array[ResearchQuote] = emptyArray

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

  $scope.getFilteredResults = () => filteredResults

  $scope.getSearchResults = () => searchResults

  $scope.getExchangeCount = (aExchange: js.UndefOr[String]) => {
    (aExchange.map(e => exchangeCounts.getOrElse(e, 0)) getOrElse 0).toDouble
  }

  $scope.getExchangeSet = (aExchange: js.UndefOr[String]) => {
    aExchange.map(e => exchangeSets.getOrElse(e, false)) getOrElse false
  }

  $scope.filterExchanges = () => syncLoading($scope, $timeout) {
    searchResults filter { q =>
      val exchange = MainController.normalizeExchange(q.exchange)
      exchangeSets.getOrElse(exchange, false)
    }
  }

  $scope.getSearchResults = () => filteredResults

  $scope.getSearchResultClass = (aCount: js.UndefOr[Int]) => aCount map { count =>
    getSearchResultClass(count)
  }

  $scope.getSearchResultsCount = () => filteredResults.length

  $scope.columnAlign = (aColumn: js.UndefOr[String]) => aColumn map { column =>
    columnAlign(column)
  }

  $scope.rowClass = (column: js.UndefOr[String], row: js.UndefOr[ResearchQuote]) => {
    if (column.contains("symbol")) row.flatMap(_.exchange) else column
  }

  $scope.research = (aSearchOptions: js.UndefOr[ResearchOptions]) => aSearchOptions foreach { searchOptions =>
    filteredResults = emptyArray
    searchResults = emptyArray

    // execute the search
    asyncLoading($scope)(researchService.search(searchOptions)) onComplete {
      case Success(response) =>
        val results = response.data
        console.log(s"${results.length} quotes found")
        val exchanges = js.Dictionary[Int]()
        results.foreach { q =>
          // normalize the exchange
          val exchange = MainController.normalizeExchange(q.exchange)
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
        $scope.$apply(() => {
          exchangeCounts = exchanges
          searchResults = results
          filteredResults = results
          $scope.filterExchanges()
        })

        // save the search options
        $cookies.putObject(CookieName, searchOptions)

      case Failure(e) =>
        console.error(s"Quote Search Failed - json => ${angular.toJson(searchOptions, pretty = false)}")
        toaster.error("Failed to execute search")
    }
  }

  //////////////////////////////////////////////////////////////////////
  //              Private Functions
  //////////////////////////////////////////////////////////////////////

  private def exchangeSets = $scope.exchangeSets

  private def getSearchResultClass(count: js.UndefOr[Int]) = {
    count.map {
      case n if n < 250 => "results_small"
      case n if n < 350 => "results_medium"
      case _ => "results_large"
    } getOrElse "results_none"
  }

  private def columnAlign(column: String) = if (column == "symbol") "left" else "right"

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

}

/**
 * Research Controller Singleton
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ResearchController {
  // data collections
  val MaxResultsSet: js.Array[Int] = js.Array(10, 25, 50, 75, 100, 150, 200, 250)
  val PriceRanges: js.Array[Int] = js.Array(0, 1, 2, 5, 10, 15, 20, 25, 30, 40, 50, 75, 100)
  val VolumeRanges: js.Array[Int] = js.Array(0, 1000, 5000, 10000, 20000, 50000, 100000, 250000, 500000, 1000000, 5000000, 10000000, 20000000, 50000000)
  val Percentages: js.Array[Int] = js.Array(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100)
  val ChangePercentages: js.Array[Int] = js.Array(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, -5, -10, -15, -25, -50, -75, -100)
  private val CookieName = "ShockTrade_Research_SearchOptions"

}

/**
 * Research Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait ResearchScope extends RootScope {
  // variables
  var exchangeSets: js.Dictionary[Boolean] = js.native
  var maxResultsSet: js.Array[Int] = js.native
  var priceRanges: js.Array[Int] = js.native
  var volumeRanges: js.Array[Int] = js.native
  var percentages: js.Array[Int] = js.native
  var changePercentages: js.Array[Int] = js.native
  var searchOptions: ResearchOptions = js.native

  // functions
  var getFilteredResults: js.Function0[js.Array[ResearchQuote]] = js.native
  var getSearchResults: js.Function0[js.Array[ResearchQuote]] = js.native
  var getExchangeCount: js.Function1[js.UndefOr[String], Double] = js.native
  var getExchangeSet: js.Function1[js.UndefOr[String], Boolean] = js.native
  var filterExchanges: js.Function0[js.Array[ResearchQuote]] = js.native
  var getSearchResultClass: js.Function1[js.UndefOr[Int], js.UndefOr[String]] = js.native
  var getSearchResultsCount: js.Function0[Int] = js.native
  var columnAlign: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native
  var rowClass: js.Function2[js.UndefOr[String], js.UndefOr[ResearchQuote], js.UndefOr[String]] = js.native
  var research: js.Function1[js.UndefOr[ResearchOptions], Unit] = js.native

}
