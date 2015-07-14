package com.shocktrade.javascript.discover

import com.github.ldaniels528.scalascript.core.TimerConversions._
import com.github.ldaniels528.scalascript.core.{Location, Q, Timeout}
import com.github.ldaniels528.scalascript.extensions.{Cookies, Toaster}
import com.github.ldaniels528.scalascript.{Scope, angular, injected, scoped}
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript._
import com.shocktrade.javascript.dialogs.{NewOrderDialogService, NewOrderParams}
import com.shocktrade.javascript.discover.DiscoverController._
import com.shocktrade.javascript.models.{FullQuote, OrderQuote}
import com.shocktrade.javascript.profile.ProfileService
import org.scalajs.dom.console

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.UndefOr
import scala.util.{Failure, Success}

/**
 * Discover Controller
 */
class DiscoverController($scope: DiscoverScope, $cookies: Cookies, $location: Location, $q: Q,
                         $routeParams: DiscoverRouteParams, $timeout: Timeout, toaster: Toaster,
                         @injected("MarketStatus") marketStatus: MarketStatusService,
                         @injected("MySession") mySession: MySession,
                         @injected("NewOrderDialog") newOrderDialog: NewOrderDialogService,
                         @injected("ProfileService") profileService: ProfileService,
                         @injected("QuoteService") quoteService: QuoteService)
  extends AutoCompletionController($scope, $q, quoteService) with GlobalLoading {

  private var usMarketStatus: Either[MarketStatus, Boolean] = Right(false)

  // setup the public variables
  $scope.ticker = null
  $scope.q = FullQuote(active = true)

  // define the display options
  $scope.options = DiscoverOptions(range = $cookies.getOrElse("chart_range", "5d"))
  $scope.expanders = Expanders

  ///////////////////////////////////////////////////////////////////////////
  //          Public Function
  ///////////////////////////////////////////////////////////////////////////

  @scoped def expandSection(module: ModuleExpander) = _expandSection(module)

  private def _expandSection(module: ModuleExpander) = module.expanded = !module.expanded

  @scoped def popupNewOrderDialog(symbol: UndefOr[String]) = newOrderDialog.popup(NewOrderParams(symbol = symbol))

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped
  def loadQuote(ticker: js.Dynamic) = {
    console.log(s"Loading symbol ${angular.toJson(ticker, pretty = false)}")

    // determine the symbol
    val symbol = (if (isDefined(ticker.symbol)) ticker.symbol.as[String].toUpperCase
    else {
      val _ticker = ticker.as[String]
      val index = _ticker.indexOf(" ")
      if (index == -1) _ticker else _ticker.substring(0, index)
    }).toUpperCase

    updateQuote(symbol)
  }

  private def updateQuote(ticker: String) {
    // get the symbol (e.g. "AAPL - Apple Inc")
    val symbol = if (ticker.contains(" ")) ticker.substring(0, ticker.indexOf(" ")).trim else ticker

    // load the quote
    asyncLoading($scope)(quoteService.getStockQuote(symbol)) onComplete {
      case Success(quote) if isDefined(quote.symbol) =>
        // capture the quote
        $scope.q = quote
        $scope.ticker = s"${quote.symbol} - ${quote.name}"

        // update the address bar
        $location.search("symbol", quote.symbol)

        // store the last symbol
        $cookies.put(LastSymbolCookie, quote.symbol)

        // add the symbol to the Recently-viewed Symbols
        mySession.addRecentSymbol(symbol)

        // load the trading history
        $scope.tradingHistory = null
        if ($scope.expanders(6).expanded) {
          _expandSection($scope.expanders(6))
        }

      case Success(quote) =>
        console.log(s"quote = ${angular.toJson(quote)}")
        toaster.warning(s"Symbol not found")

      case Failure(e) =>
        g.console.error(s"Failed to retrieve quote: ${e.getMessage}")
        toaster.error(s"Error loading quote $symbol")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          ETF Holdings / Products
  ///////////////////////////////////////////////////////////////////////////

  @scoped def hasHoldings(q: FullQuote) = q.legalType.exists(_ == "ETF") && q.products.exists(_.nonEmpty)

  ///////////////////////////////////////////////////////////////////////////
  //          Symbols - Favorites
  ///////////////////////////////////////////////////////////////////////////

  @scoped
  def addFavoriteSymbol(aSymbol: UndefOr[String]) = {
    for {
      symbol <- aSymbol.toOption
      userId <- mySession.userProfile.OID_?
    } yield profileService.addFavoriteSymbol(userId, symbol)
  }

  @scoped def isFavorite(symbol: UndefOr[String]) = symbol.exists(mySession.isFavoriteSymbol)

  @scoped
  def removeFavoriteSymbol(aSymbol: UndefOr[String]) = {
    for {
      symbol <- aSymbol.toOption
      userId <- mySession.userProfile.OID_?
    } yield profileService.removeFavoriteSymbol(userId, symbol)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Symbols - Recent
  ///////////////////////////////////////////////////////////////////////////

  @scoped
  def addRecentSymbol(aSymbol: UndefOr[String]) = {
    for {
      symbol <- aSymbol.toOption
      userId <- mySession.userProfile.OID_?
    } yield profileService.addRecentSymbol(userId, symbol)
  }

  @scoped def isRecentSymbol(symbol: UndefOr[String]) = symbol.exists(mySession.isRecentSymbol)

  @scoped
  def removeRecentSymbol(aSymbol: UndefOr[String]) = {
    for {
      symbol <- aSymbol.toOption
      userId <- mySession.userProfile.OID_?
    } yield profileService.removeRecentSymbol(userId, symbol)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Risk Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped
  def getBetaClass(beta: UndefOr[Double]) = beta map {
    case b if b > 1.3 || b < -1.3 => "volatile_red"
    case b if b >= 0.0 => "volatile_green"
    case b if b < 0 => "volatile_yellow"
    case _ => js.undefined
  } getOrElse js.undefined

  @scoped
  def getRiskClass(riskLevel: UndefOr[String]) = riskLevel map {
    case rs if isDefined(rs) && rs.nonBlank => s"risk_${rs.toLowerCase}"
    case _ => js.undefined
  } getOrElse js.undefined

  @scoped
  def getRiskDescription(riskLevel: UndefOr[String]) = riskLevel map {
    case "Low" => "Generally recommended for investment"
    case "Medium" => "Not recommended for inexperienced investors"
    case "High" => "Not recommended for investment"
    case "Unknown" => "The risk level could not be determined"
    case _ => "The risk level could not be determined"
  } getOrElse js.undefined

  ///////////////////////////////////////////////////////////////////////////
  //          Market Status Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped
  def isUSMarketsOpen: UndefOr[Boolean] = {
    usMarketStatus match {
      case Left(status) => status.active
      case Right(loading) =>
        if (!loading) {
          usMarketStatus = Right(true)
          console.log("Retrieving market status...")
          marketStatus.getMarketStatus onComplete {
            case Success(status) =>
              // {"stateChanged":false,"active":false,"sysTime":1392092448795,"delay":-49848795,"start":1392042600000,"end":1392066000000}
              // retrieve the delay in milliseconds from the server
              var delay = status.delay
              if (delay < 0) {
                delay = Math.max(status.end - status.sysTime, 300000)
              }

              // set the market status
              console.log(s"US Markets are ${if (status.active) "Open" else "Closed"}; Waiting for $delay msec until next trading start...")
              usMarketStatus = Left(status)

              // update the status after delay
              console.log(s"Re-loading market status in ${status.delay.minutes}")
              $timeout(() => usMarketStatus = Right(false), 5.minutes)

            case Failure(e) =>
              toaster.error("Failed to retrieve market status")
              g.console.error(s"Failed to retrieve market status: ${e.getMessage}")
          }
        }
        js.undefined
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // load the symbol
  if (!isDefined($scope.q.symbol)) {
    // get the symbol
    val symbol = $routeParams.symbol getOrElse $cookies.getOrElse(LastSymbolCookie, mySession.getMostRecentSymbol)

    // load the symbol
    updateQuote(symbol)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  // setup the chart range
  $scope.$watch("options.range", (newValue: UndefOr[Any], oldValue: js.Any) => newValue.foreach($cookies.put("chart_range", _)))

}

/**
 * Discover Controller
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
  val Expanders = js.Array(
    ModuleExpander(title = "Performance & Risk",
      url = "/assets/views/discover/quotes/expanders/price_performance.htm",
      icon = "fa-line-chart",
      expanded = false,
      visible = isPerformanceRisk),
    ModuleExpander(title = "Income Statement",
      url = "/assets/views/discover/quotes/expanders/income_statement.htm",
      icon = "fa-money",
      expanded = false,
      visible = isIncomeStatement),
    ModuleExpander(title = "Balance Sheet",
      url = "/assets/views/discover/quotes/expanders/balanace_sheet.htm",
      icon = "fa-calculator",
      expanded = false,
      visible = isBalanceSheet),
    ModuleExpander(title = "Valuation Measures",
      url = "/assets/views/discover/quotes/expanders/valuation_measures.htm",
      icon = "fa-gears",
      expanded = false,
      visible = isValuationMeasures),
    ModuleExpander(title = "Share Statistics",
      url = "/assets/views/discover/quotes/expanders/share_statistics.htm",
      icon = "fa-bar-chart",
      expanded = false,
      visible = isShareStatistics),
    ModuleExpander(title = "Dividends & Splits",
      url = "/assets/views/discover/quotes/expanders/dividends_splits.htm",
      icon = "fa-cut",
      expanded = false,
      visible = isDividendsSplits),
    ModuleExpander(title = "Historical Quotes",
      url = "/assets/views/discover/quotes/trading_history.htm",
      icon = "fa-calendar",
      expanded = false))

}

/**
 * Discover Options
 */
trait DiscoverOptions extends js.Object {
  var range: String = js.native
}

/**
 * Discover Options Singleton
 */
object DiscoverOptions {

  def apply(range: String) = {
    val options = makeNew[DiscoverOptions]
    options.range = range
    options
  }

}

/**
 * Discover Route Parameters
 */
trait DiscoverRouteParams extends js.Object {
  var symbol: UndefOr[String] = js.native

}

/**
 * Discover Scope
 */
trait DiscoverScope extends Scope {
  var expanders: js.Array[ModuleExpander] = js.native
  var options: DiscoverOptions = js.native
  var q: OrderQuote = js.native
  var ticker: String = js.native
  var tradingHistory: js.Array[js.Object] = js.native

}