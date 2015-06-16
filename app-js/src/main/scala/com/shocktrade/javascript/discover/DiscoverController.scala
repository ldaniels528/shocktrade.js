package com.shocktrade.javascript.discover

import biz.enef.angulate.core.{Location, Timeout}
import biz.enef.angulate.{ScopeController, angular, named}
import com.greencatsoft.angularjs.core.Q
import com.ldaniels528.angularjs.{CookieStore, Toaster}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.discover.DiscoverController._
import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Discover Controller
 * @author lawrence.daniels@gmail.com
 */
class DiscoverController($scope: js.Dynamic, $cookieStore: CookieStore, $interval: Timeout, $location: Location, $q: Q,
                         $routeParams: js.Dynamic, $timeout: Timeout, toaster: Toaster,
                         @named("MarketStatus") marketStatus: MarketStatusService,
                         @named("MySession") mySession: MySession,
                         @named("NewOrderDialog") newOrderDialog: js.Dynamic,
                         @named("QuoteService") quoteService: QuoteService)
  extends ScopeController {

  // setup the public variables
  $scope.marketClock = new js.Date().toTimeString()
  $scope.ticker = null
  $scope.q = JS(active = true)

  // define the display options
  $scope.options = JS(range = $cookieStore.getOrElse("chart_range", "5d"))
  $scope.expanders = expanders

  /**
   * Initializes the module
   */
  $scope.init = () => {
    // setup market status w/updates
    $interval(() => $scope.marketClock = new js.Date().toTimeString(), 1.second)

    // setup the market status updates
    setupMarketStatusUpdates()
  }

  $scope.autoCompleteSymbols = (searchTerm: String) => {
    val deferred = $q.defer()
    quoteService.autoCompleteSymbols(searchTerm, 20) onComplete {
      case Success(response) => deferred.resolve(response)
      case Failure(e) => deferred.reject(e.getMessage)
    }
    deferred.promise
  }

  $scope.expandSection = (module: js.Dynamic) => module.expanded = !module.expanded

  $scope.getMatchedAssetIcon = (q: js.Dynamic) => "/assets/images/asset_types/stock.png"

  $scope.popupNewOrderDialog = (symbol: String) => newOrderDialog.popup(JS(symbol = symbol))

  ///////////////////////////////////////////////////////////////////////////
  //          Quotes Loading
  ///////////////////////////////////////////////////////////////////////////

  $scope.loadTickerQuote = (_ticker: String) => {
    val stockTicker = jQuery("#stockTicker").value()
    val ticker = if (isDefined(stockTicker)) stockTicker.as[String] else _ticker
    $scope.loadQuote(ticker)
  }

  $scope.loadQuote = (ticker: js.Dynamic) => {
    g.console.log(s"Loading symbol ${angular.toJson(ticker, pretty = false)}")

    // setup the loading animation
    $scope.startLoading()

    // determine the symbol
    val symbol = (if (isDefined(ticker.symbol)) ticker.symbol.as[String]
    else {
      val _ticker = ticker.as[String]
      val index = _ticker.indexOf(" ")
      if (index == -1) _ticker else _ticker.substring(0, index)
    }).toUpperCase

    // load the quote
    quoteService.getStockQuote(symbol) onComplete {
      case Success(quote) =>
        // capture the quote
        $scope.q = quote
        $scope.ticker = s"${quote.symbol} - ${quote.name}"

        $location.search("symbol", quote.symbol)

        // store the last symbol
        $cookieStore.put("QuoteService_lastSymbol", quote.symbol)

        // add the symbol to the Recently-viewed Symbols
        mySession.addRecentSymbol(symbol)

        // get the risk level
        quoteService.getRiskLevel(symbol) onComplete {
          case Success(response) => quote.riskLevel = response
          case Failure(e) =>
            toaster.error("Error!", "Error retrieving risk level for " + symbol)
        }

        // load the trading history
        $scope.tradingHistory = null
        val expanders = $scope.expanders.asArray[js.Dynamic]
        if (isDefined(expanders(6).expanded)) {
          $scope.expandSection(expanders(6))
        }

        // disabling the loading status
        $scope.stopLoading()

      case Failure(e) =>
        g.console.error(s"Failed to retrieve quote: ${e.getMessage}")
        $scope.stopLoading()
        toaster.error("Error!", "Error loading quote " + symbol)
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          ETF Holdings / Products
  ///////////////////////////////////////////////////////////////////////////

  $scope.hasHoldings = (q: js.Dynamic) => isDefined(q) && isDefined(q.products) && (q.legalType === "ETF") && q.products.asArray[js.Dynamic].nonEmpty

  ///////////////////////////////////////////////////////////////////////////
  //          Symbols - Favorites
  ///////////////////////////////////////////////////////////////////////////

  $scope.addFavoriteSymbol = (symbol: String) => mySession.addFavoriteSymbol(symbol)

  $scope.isFavorite = (symbol: js.UndefOr[String]) => symbol.exists(mySession.isFavoriteSymbol)

  $scope.removeFavoriteSymbol = (symbol: String) => mySession.removeFavoriteSymbol(symbol)

  ///////////////////////////////////////////////////////////////////////////
  //          Symbols - Recent
  ///////////////////////////////////////////////////////////////////////////

  $scope.addRecentSymbol = (symbol: String) => {
    if (mySession.isAuthenticated() && !mySession.isRecentSymbol(symbol)) {
      mySession.addRecentSymbol(symbol)
    }
  }

  $scope.isRecentSymbol = (symbol: js.UndefOr[String]) => symbol.exists(mySession.isRecentSymbol)

  $scope.removeRecentSymbol = (symbol: String) => mySession.removeRecentSymbol(symbol)

  ///////////////////////////////////////////////////////////////////////////
  //          Risk Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getBetaClass = (beta: js.UndefOr[java.lang.Double]) => {
    beta map {
      case b if b > 1.3 || b < -1.3 => "volatile_red"
      case b if b >= 0.0 => "volatile_green"
      case b if b < 0 => "volatile_yellow"
      case _ => ""
    } getOrElse ""
  }

  $scope.getRiskClass = (riskLevel: js.UndefOr[String]) => riskLevel map {
    case rs if rs != null && rs.nonBlank => s"risk_${rs.toLowerCase}"
    case _ => null
  }

  $scope.getRiskDescription = (riskLevel: js.UndefOr[String]) => {
    riskLevel map {
      case "Low" => "Generally recommended for investment"
      case "Medium" => "Not recommended for inexperienced investors"
      case "High" => "Not recommended for investment"
      case "Unknown" => "The risk level could not be determined"
      case _ => "The risk level could not be determined"
    } getOrElse ""
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Market Status Functions
  ///////////////////////////////////////////////////////////////////////////

  private def setupMarketStatusUpdates() {
    $scope.usMarketsOpen = null
    g.console.log("Retrieving market status...")
    marketStatus.getMarketStatus onComplete {
      case Success(status) =>
        // {"stateChanged":false,"active":false,"sysTime":1392092448795,"delay":-49848795,"start":1392042600000,"end":1392066000000}
        // retrieve the delay in milliseconds from the server
        var delay = status.delay
        if (delay < 0) {
          delay = Math.max(status.end - status.sysTime, 5.minutes)
        }

        // set the market status
        g.console.log(s"US Markets are ${if (status.active) "Open" else "Closed"}; Waiting for $delay msec until next trading start...")

        // set the status after 750ms
        $timeout(() => $scope.usMarketsOpen = status.active, 750.milliseconds)

        // wait for the delay, then call recursively
        $timeout(() => setupMarketStatusUpdates(), delay.toInt)

      case Failure(e) =>
        toaster.error("Failed to retrieve market status")
        g.console.error(s"Failed to retrieve market status: ${e.getMessage}")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // load the symbol
  if (!isDefined($scope.q.symbol)) {
    // get the symbol
    val symbol = if (isDefined($routeParams.symbol)) $routeParams.symbol.as[String]
    else $cookieStore.getOrElse("QuoteService_lastSymbol", mySession.getMostRecentSymbol())

    // load the symbol
    $scope.loadQuote(symbol)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  // setup the chart range
  $scope.$watch("options.range", (newValue: js.Dynamic, oldValue: js.Dynamic) => $cookieStore.put("chart_range", newValue))

}

/**
 * Discover Controller
 * @author lawrence.daniels@gmail.com
 */
object DiscoverController {

  def isPerformanceRisk: js.Function1[js.Dynamic, Boolean] = (q: js.Dynamic) => {
    isDefined(q.high52Week) || isDefined(q.low52Week) || isDefined(q.change52Week) ||
      isDefined(q.movingAverage50Day) || isDefined(q.movingAverage200Day) ||
      isDefined(q.change52WeekSNP500) || isDefined(q.beta)
  }

  def isIncomeStatement: js.Function1[js.Dynamic, Boolean] = (q: js.Dynamic) => {
    isDefined(q.revenue) || isDefined(q.revenuePerShare) || isDefined(q.revenueGrowthQuarterly) ||
      isDefined(q.grossProfit) || isDefined(q.EBITDA) || isDefined(q.netIncomeAvailToCommon) ||
      isDefined(q.dilutedEPS) || isDefined(q.earningsGrowthQuarterly)
  }

  def isBalanceSheet: js.Function1[js.Dynamic, Boolean] = (q: js.Dynamic) => {
    isDefined(q.totalCash) || isDefined(q.totalDebt) || isDefined(q.currentRatio) ||
      isDefined(q.totalCashPerShare) || isDefined(q.totalDebtOverEquity) || isDefined(q.bookValuePerShare) ||
      isDefined(q.returnOnAssets) || isDefined(q.profitMargin) || isDefined(q.mostRecentQuarterDate) ||
      isDefined(q.returnOnEquity) || isDefined(q.operatingMargin) || isDefined(q.fiscalYearEndDate)
  }

  def isValuationMeasures: js.Function1[js.Dynamic, Boolean] = (q: js.Dynamic) => {
    isDefined(q.enterpriseValue) || isDefined(q.trailingPE) || isDefined(q.forwardPE) ||
      isDefined(q.pegRatio) || isDefined(q.priceOverSales) || isDefined(q.priceOverBookValue) ||
      isDefined(q.enterpriseValueOverRevenue) || isDefined(q.enterpriseValueOverEBITDA) ||
      isDefined(q.operatingCashFlow) || isDefined(q.leveredFreeCashFlow)
  }

  def isShareStatistics: js.Function1[js.Dynamic, Boolean] = (q: js.Dynamic) => {
    isDefined(q.avgVolume3Month) || isDefined(q.avgVolume10Day) || isDefined(q.sharesOutstanding) ||
      isDefined(q.sharesFloat) || isDefined(q.pctHeldByInsiders) || isDefined(q.pctHeldByInstitutions) ||
      isDefined(q.sharesShort) || isDefined(q.shortRatio) || isDefined(q.shortPctOfFloat) ||
      isDefined(q.sharesShortPriorMonth)
  }

  def isDividendsSplits: js.Function1[js.Dynamic, Boolean] = (q: js.Dynamic) => {
    isDefined(q.forwardAnnualDividendRate) || isDefined(q.forwardAnnualDividendYield) ||
      isDefined(q.trailingAnnualDividendYield) || isDefined(q.divYield5YearAvg) ||
      isDefined(q.payoutRatio) || isDefined(q.dividendDate) || isDefined(q.exDividendDate) ||
      isDefined(q.lastSplitFactor) || isDefined(q.lastSplitDate)
  }

  // define the Quote module expanders
  val expanders = js.Array(
    JS(title = "Performance & Risk",
      url = "/assets/views/discover/quotes/expanders/price_performance.htm",
      icon = "fa-line-chart",
      expanded = false,
      visible = isPerformanceRisk),
    JS(title = "Income Statement",
      url = "/assets/views/discover/quotes/expanders/income_statement.htm",
      icon = "fa-money",
      expanded = false,
      visible = isIncomeStatement),
    JS(title = "Balance Sheet",
      url = "/assets/views/discover/quotes/expanders/balanace_sheet.htm",
      icon = "fa-calculator",
      expanded = false,
      visible = isBalanceSheet),
    JS(title = "Valuation Measures",
      url = "/assets/views/discover/quotes/expanders/valuation_measures.htm",
      icon = "fa-gears",
      expanded = false,
      visible = isValuationMeasures),
    JS(title = "Share Statistics",
      url = "/assets/views/discover/quotes/expanders/share_statistics.htm",
      icon = "fa-bar-chart",
      expanded = false,
      visible = isShareStatistics),
    JS(title = "Dividends & Splits",
      url = "/assets/views/discover/quotes/expanders/dividends_splits.htm",
      icon = "fa-cut",
      expanded = false,
      visible = isDividendsSplits),
    JS(title = "Historical Quotes",
      url = "/assets/views/discover/quotes/trading_history.htm",
      icon = "fa-calendar",
      expanded = false))

}