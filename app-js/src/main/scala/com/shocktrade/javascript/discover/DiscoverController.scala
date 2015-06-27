package com.shocktrade.javascript.discover

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.core.Angular.angular
import com.ldaniels528.javascript.angularjs.core.{Controller, Location, Q, Timeout}
import com.ldaniels528.javascript.angularjs.extensions.{Cookies, Toaster}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.NewOrderDialogService
import com.shocktrade.javascript.discover.DiscoverController._
import com.shocktrade.javascript.discover.MarketStatusService.MarketStatus
import com.shocktrade.javascript.profile.ProfileService
import org.scalajs.jquery.jQuery

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Discover Controller
 * @author lawrence.daniels@gmail.com
 */
class DiscoverController($scope: js.Dynamic, $cookieStore: Cookies, $interval: Timeout, $location: Location,
                         $q: Q, $routeParams: js.Dynamic, $timeout: Timeout, toaster: Toaster,
                         @named("MarketStatus") marketStatus: MarketStatusService,
                         @named("MySession") mySession: MySession,
                         @named("NewOrderDialog") newOrderDialog: NewOrderDialogService,
                         @named("ProfileService") profileService: ProfileService,
                         @named("QuoteService") quoteService: QuoteService)
  extends Controller {

  private var usMarketStatus: Either[MarketStatus, Boolean] = Right(false)

  // setup the public variables
  $scope.marketClock = new js.Date().toTimeString()
  $scope.ticker = null
  $scope.q = JS(active = true)

  // define the display options
  $scope.options = JS(range = $cookieStore.getOrElse("chart_range", "5d"))
  $scope.expanders = expanders

  ///////////////////////////////////////////////////////////////////////////
  //          Public Function
  ///////////////////////////////////////////////////////////////////////////

  $scope.init = () => init()

  $scope.isUSMarketsOpen = () => isUSMarketsOpen

  $scope.autoCompleteSymbols = (searchTerm: String) => autoCompleteSymbols(searchTerm)

  $scope.expandSection = (module: js.Dynamic) => module.expanded = !module.expanded

  $scope.getBetaClass = (beta: js.UndefOr[java.lang.Double]) => getBetaClass(beta)

  $scope.getRiskClass = (riskLevel: js.UndefOr[String]) => getRiskClass(riskLevel)

  $scope.getRiskDescription = (riskLevel: js.UndefOr[String]) => getRiskDescription(riskLevel)

  $scope.loadQuote = (ticker: js.Dynamic) => loadQuote(ticker)

  $scope.loadTickerQuote = (ticker: String) => getTickerQuote(ticker)

  $scope.popupNewOrderDialog = (symbol: js.UndefOr[String]) => newOrderDialog.popup(JS(symbol = symbol))

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Initializes the module
   */
  private def init() = {
    // setup market status w/updates
    $interval(() => $scope.marketClock = new js.Date().toTimeString(), 1.second)
  }

  private def autoCompleteSymbols(searchTerm: String) = {
    val deferred = $q.defer[js.Array[js.Dynamic]]()
    quoteService.autoCompleteSymbols(searchTerm, 20) onComplete {
      case Success(response) => deferred.resolve(response)
      case Failure(e) => deferred.reject(e.getMessage)
    }
    deferred.promise
  }

  private def getTickerQuote(_ticker: String) = {
    val stockTicker = jQuery("#stockTicker").value()
    val ticker = if (isDefined(stockTicker)) stockTicker.as[String] else _ticker
    getQuote(ticker)
  }

  private def loadQuote(ticker: js.Dynamic) = {
    g.console.log(s"Loading symbol ${angular.toJson(ticker, pretty = false)}")

    // determine the symbol
    val symbol = (if (isDefined(ticker.symbol)) ticker.symbol.as[String]
    else {
      val _ticker = ticker.as[String]
      val index = _ticker.indexOf(" ")
      if (index == -1) _ticker else _ticker.substring(0, index)
    }).toUpperCase

    getQuote(symbol)
  }

  private def getQuote(symbol: String) = {
    // load the quote
    asyncLoading($scope)(quoteService.getStockQuote(symbol)) onComplete {
      case Success(quote) =>
        // capture the quote
        $scope.q = quote
        $scope.ticker = s"${quote.symbol} - ${quote.name}"

        $location.search("symbol", quote.symbol)

        // store the last symbol
        $cookieStore.put(LastSymbolCookie, quote.symbol)

        // add the symbol to the Recently-viewed Symbols
        mySession.addRecentSymbol(symbol)

        // load the trading history
        $scope.tradingHistory = null
        val expanders = $scope.expanders.asArray[js.Dynamic]
        if (expanders(6).expanded.isTrue) {
          $scope.expandSection(expanders(6))
        }

      case Failure(e) =>
        g.console.error(s"Failed to retrieve quote: ${e.getMessage}")
        toaster.error(s"Error loading quote $symbol")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          ETF Holdings / Products
  ///////////////////////////////////////////////////////////////////////////

  $scope.hasHoldings = (q: js.Dynamic) => isDefined(q) && isDefined(q.products) && (q.legalType === "ETF") && q.products.asArray[js.Dynamic].nonEmpty

  ///////////////////////////////////////////////////////////////////////////
  //          Symbols - Favorites
  ///////////////////////////////////////////////////////////////////////////

  $scope.addFavoriteSymbol = (symbol: String) => profileService.addFavoriteSymbol(mySession.getUserID(), symbol)

  $scope.isFavorite = (symbol: js.UndefOr[String]) => symbol.exists(mySession.isFavoriteSymbol)

  $scope.removeFavoriteSymbol = (symbol: String) => profileService.removeFavoriteSymbol(mySession.getUserID(), symbol)

  ///////////////////////////////////////////////////////////////////////////
  //          Symbols - Recent
  ///////////////////////////////////////////////////////////////////////////

  $scope.addRecentSymbol = (symbol: String) => {
    if (mySession.isAuthenticated() && !mySession.isRecentSymbol(symbol)) {
      profileService.addRecentSymbol(mySession.getUserID(), symbol)
    }
  }

  $scope.isRecentSymbol = (symbol: js.UndefOr[String]) => symbol.exists(mySession.isRecentSymbol)

  $scope.removeRecentSymbol = (symbol: String) => profileService.removeRecentSymbol(mySession.getUserID(), symbol)

  ///////////////////////////////////////////////////////////////////////////
  //          Risk Functions
  ///////////////////////////////////////////////////////////////////////////

  private def getBetaClass(beta: js.UndefOr[java.lang.Double]) = {
    beta map {
      case b if b > 1.3 || b < -1.3 => "volatile_red"
      case b if b >= 0.0 => "volatile_green"
      case b if b < 0 => "volatile_yellow"
      case _ => ""
    } getOrElse ""
  }

  private def getRiskClass(riskLevel: js.UndefOr[String]) = riskLevel map {
    case rs if rs != null && rs.nonBlank => s"risk_${rs.toLowerCase}"
    case _ => null
  }

  private def getRiskDescription(riskLevel: js.UndefOr[String]) = {
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

  private def isUSMarketsOpen: java.lang.Boolean = {
    usMarketStatus match {
      case Left(status) => status.active
      case Right(loading) =>
        if (!loading) {
          usMarketStatus = Right(true)
          g.console.log("Retrieving market status...")
          marketStatus.getMarketStatus onComplete {
            case Success(status) =>
              // {"stateChanged":false,"active":false,"sysTime":1392092448795,"delay":-49848795,"start":1392042600000,"end":1392066000000}
              // retrieve the delay in milliseconds from the server
              var delay = status.delay
              if (delay < 0) {
                delay = Math.max(status.end - status.sysTime, 300000)
              }

              // set the market status
              g.console.log(s"US Markets are ${if (status.active) "Open" else "Closed"}; Waiting for $delay msec until next trading start...")
              usMarketStatus = Left(status)

              // update the status after delay
              g.console.log(s"Re-loading market status in ${status.delay.minutes}")
              $timeout(() => usMarketStatus = Right(false), 300000)

            case Failure(e) =>
              toaster.error("Failed to retrieve market status")
              g.console.error(s"Failed to retrieve market status: ${e.getMessage}")
          }
        }
        null
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // load the symbol
  if (!isDefined($scope.q.symbol)) {
    // get the symbol
    val symbol = $routeParams.symbol.toUndefOr[String] getOrElse $cookieStore.getOrElse(LastSymbolCookie, mySession.getMostRecentSymbol())

    // load the symbol
    getQuote(symbol)
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
  val LastSymbolCookie = "QuoteService_lastSymbol"

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