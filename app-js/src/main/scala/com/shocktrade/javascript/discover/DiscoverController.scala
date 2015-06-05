package com.shocktrade.javascript.discover

import com.greencatsoft.angularjs.core.{Scope, _}
import com.greencatsoft.angularjs.{AbstractController, injectable}
import com.ldaniels528.angularjs.{RouteParams, CookieStore, Toaster}
import com.shocktrade.javascript.app.model.DetailedQuote
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.dialogs.NewOrderDialog
import com.shocktrade.javascript.discover.DiscoverController._
import org.scalajs.dom

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Any.fromString
import scala.scalajs.js.JSON
import scala.scalajs.js.UndefOr.undefOr2ops
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.timers._
import scala.util.{Failure, Success}

/**
 * Discover Controller
 * @author lawrence.daniels@gmail.com
 */
@JSExport
@injectable("DiscoverController")
class DiscoverController($scope: DiscoverScope,
                         $log: Log,
                         $routeParams: RouteParams,
                         $cookieStore: CookieStore,
                         toaster: Toaster,
                         contestService: ContestService,
                         favoriteSymbols: FavoriteSymbols,
                         quoteService: QuoteService,
                         recentSymbols: RecentSymbols)
  extends AbstractController[DiscoverScope]($scope) {

  $scope.options.put("range", $cookieStore.get("chart_range").toOption.getOrElse("5d"))

  // setup the chart range
  $scope.$watch("options.range", $scope.options.get("range").foreach(range => $cookieStore.put("chart_range", range)))

  @JSExport
  def autoCompleteSymbols(searchTerm: String) = quoteService.autoCompleteSymbols(searchTerm, 20)

  /**
   * Initializes the module
   */
  @JSExport
  def init() {
    // setup market status w/updates
    setInterval(1.second)($scope.marketClock = new js.Date().toTimeString())

    // setup the market status updates
    setupMarketStatusUpdates()
  }

  @JSExport
  def popupNewOrderDialog(symbol: String) = NewOrderDialog.popup(js.Dictionary("symbol" -> symbol))

  @JSExport
  def expandSection(module: Expander) {
    module.expanded = !module.expanded
    if (module.expanded && module.onExpand != null) {
      module.onExpand($scope.quote, module)
    }
  }

  @JSExport
  def addFavoriteSymbol(symbol: String) = favoriteSymbols.add(symbol)

  @JSExport
  def isFavorite(symbol: String) = favoriteSymbols.isFavorite(symbol)

  @JSExport
  def removeFavoriteSymbol(symbol: String) = favoriteSymbols.remove(symbol)

  @JSExport
  def hasHoldings(quote: DetailedQuote) = {
    Option(quote).exists(q => q.products.nonEmpty && (q.legalType == "ETF"))
  }

  def removeRecentSymbol(symbol: String) = recentSymbols.remove(symbol)

  @JSExport
  def getMatchedAssetIcon(q: js.Any) = {
    //console.log("q = " + angular.toJson(q))
    "/assets/images/asset_types/stock.png"
  }

  @JSExport
  def getRiskClass(riskLevel: String): String = {
    if (riskLevel.nonEmpty) "risk_" + riskLevel.toLowerCase else ""
  }

  @JSExport
  def getRiskDescription(riskLevel: String): String = {
    riskLevel match {
      case "Low" => "Generally recommended for investment"
      case "Medium" => "Not recommended for inexperienced investors"
      case "High" => "Not recommended for investment"
      case "Unknown" => "The risk level could not be determined"
      case _ => "The risk level could not be determined"
    }
  }

  @JSExport
  def getBetaClass(beta: js.UndefOr[Double]): String = {
    beta.map {
      case b if b > 1.3 || b < -1.3 => "volatile_red"
      case b if b >= 0.0 => "volatile_green"
      case b if b < 0 => "volatile_yellow"
    } getOrElse ""
  }

  def loadTickerQuote(_ticker: Ticker) {
    val ticker = dom.document.getElementById("stockTicker").textContent
    loadQuote(ticker)
  }

  @JSExport
  def loadQuote(ticker: String) = {
    $log.info("Loading symbol " + JSON.stringify(ticker))
    val symbol = extractSymbol(ticker)

    // setup the loading animation
    $scope.startLoading()

    // load the quote
    quoteService.loadStockQuote(symbol).onComplete {
      case Success(quote) =>
        // capture the quote
        $scope.quote = quote
        $scope.ticker = s"${quote.symbol} - ${quote.name}"

        // save the cookie
        $cookieStore.put("symbol", quote.symbol)

        // add the symbol to the Recently-viewed Symbols
        recentSymbols.add(symbol)

        // get the risk level
        /*
        quoteService.getRiskLevel(symbol).onComplete {
          case Success(riskLevel) =>
            quote.riskLevel = riskLevel
          case Failure(e) =>
            toaster.pop("error", "Error!", s"Error retrieving risk level for $symbol")
        }*/

        // load the trading history
        $scope.tradingHistory = null
        if (expanders(6).expanded) {
          expandSection(expanders(6))
        }

        // disabling the loading status
        $scope.stopLoading()

      case Failure(e) =>
        $log.error(s"Failed to retrieve quote: ${e.getMessage}")
        $scope.stopLoading()
        toaster.pop("error", "Error!", "Error loading quote " + symbol)
    }
  }

  private def extractSymbol(ticker: String) = {
    /*
    // determine the symbol
    val symbol =
      if (ticker.symbol != null) ticker.symbol.trim.toUpperCase
      else {
        val index = ticker.indexOf(" ")
        symbol = (index == -1 ? ticker: ticker.substring(0, index)).toUpperCase()
      }*/
    ticker
  }

  def loadTradingHistory(symbol: String, module: Expander) {
    // turn on the loading flag with timeout
    module.loading = true
    val promise = setTimeout(5.seconds)(module.loading = false)

    quoteService.getTradingHistory(symbol).onComplete {
      case Success(results) =>
        $scope.tradingHistory = results.asInstanceOf[js.Array[js.Any]]
        module.loading = false
        clearTimeout(promise)

      case Failure(e) =>
        toaster.pop("error", "Error!", s"Error loading trading history for $symbol")
    }
  }

  def tradingActive(time: js.Date) = js.Date.now()

  def selectTradingHistory(t: js.Any) = $scope.selectedTradingHistory = t

  def hasSelectedTradingHistory: Boolean = $scope.selectedTradingHistory != null

  def isSelectedTradingHistory(t: js.Any): Boolean = $scope.selectedTradingHistory == t

  def setupMarketStatusUpdates() {
    $log.info("Retrieving market status...")
    quoteService.getMarketStatus.onComplete {
      case Success(response) =>
        // retrieve the delay in milliseconds from the server
        var delay = response.delay
        if (delay < 0) {
          delay = response.end - response.sysTime
          if (delay <= 300000) {
            delay = 300000 // 5 minutes
          }
        }

        // set the market status
        $log.info(s"US Markets are ${if (response.active) "Open " else "Closed "} Waiting for $delay msec until next trading start...")
        setTimeout(750) {
          $scope.usMarketsOpen = response.active
        }

        // wait for the delay, then call recursively
        setTimeout(delay) {
          setupMarketStatusUpdates()
        }
      case Failure(e) =>
        toaster.pop("error", "Failed to retrieve current market status", null)
    }
  }

  def getExpanders = expanders

  ///////////////////////////////////////////////////////////////////////////
  //          Quote Module Collapse/Expand
  ///////////////////////////////////////////////////////////////////////////

  // define the Quote module expanders
  val expanders = List(
    Expander(
      title = "Performance & Risk",
      url = "/assets/views/discover/quotes/expanders/price_performance.htm",
      icon = "fa-line-chart",
      expanded = false,
      visible = isPerformRiskVisible),
    Expander(
      title = "Income Statement",
      url = "/assets/views/discover/quotes/expanders/income_statement.htm",
      icon = "fa-money",
      expanded = false,
      visible = isIncomeStatementVisible),
    Expander(
      title = "Balance Sheet",
      url = "/assets/views/discover/quotes/expanders/balanace_sheet.htm",
      icon = "fa-calculator",
      expanded = false,
      visible = isBalanceSheetVisible),
    Expander(
      title = "Valuation Measures",
      url = "/assets/views/discover/quotes/expanders/valuation_measures.htm",
      icon = "fa-gears",
      expanded = false,
      visible = isValuationMeasures),
    Expander(
      title = "Share Statistics",
      url = "/assets/views/discover/quotes/expanders/share_statistics.htm",
      icon = "fa-bar-chart",
      expanded = false,
      visible = isShareStatisticsVisible),
    Expander(
      title = "Dividends & Splits",
      url = "/assets/views/discover/quotes/expanders/dividends_splits.htm",
      icon = "fa-cut",
      expanded = false,
      visible = isDividendsSplitsVisible),
    Expander(
      title = "Historical Quotes",
      url = "/assets/views/discover/quotes/trading_history.htm",
      icon = "fa-calendar",
      expanded = false,
      visible = DetailedQuote => true,
      onExpand = onTransactionHistory))

  private def isPerformRiskVisible(q: js.Dynamic) = {
    !js.isUndefined(q.high52Week) || !js.isUndefined(q.low52Week) || !js.isUndefined(q.change52Week) ||
      !js.isUndefined(q.movingAverage50Day) || !js.isUndefined(q.movingAverage200Day) ||
      !js.isUndefined(q.change52WeekSNP500) || !js.isUndefined(q.beta)
  }

  private def isIncomeStatementVisible(q: js.Dynamic) = {
    !js.isUndefined(q.revenue) || !js.isUndefined(q.revenuePerShare) || !js.isUndefined(q.revenueGrowthQuarterly) ||
      !js.isUndefined(q.grossProfit) || !js.isUndefined(q.EBITDA) || !js.isUndefined(q.netIncomeAvailToCommon) ||
      !js.isUndefined(q.dilutedEPS) || !js.isUndefined(q.earningsGrowthQuarterly)
  }

  private def isBalanceSheetVisible(q: js.Dynamic) = {
    !js.isUndefined(q.totalCash) || !js.isUndefined(q.totalDebt) || !js.isUndefined(q.currentRatio) || !js.isUndefined(q.totalCashPerShare) ||
      !js.isUndefined(q.totalDebtOverEquity) || !js.isUndefined(q.bookValuePerShare) || !js.isUndefined(q.returnOnAssets) ||
      !js.isUndefined(q.profitMargin) || !js.isUndefined(q.mostRecentQuarterDate) || !js.isUndefined(q.returnOnEquity) ||
      !js.isUndefined(q.operatingMargin) || !js.isUndefined(q.fiscalYearEndDate)
  }

  private def isValuationMeasures(q: js.Dynamic) = {
    !js.isUndefined(q.enterpriseValue) || !js.isUndefined(q.trailingPE) || !js.isUndefined(q.forwardPE) || !js.isUndefined(q.pegRatio) ||
      !js.isUndefined(q.priceOverSales) || !js.isUndefined(q.priceOverBookValue) || !js.isUndefined(q.enterpriseValueOverRevenue) ||
      !js.isUndefined(q.enterpriseValueOverEBITDA) || !js.isUndefined(q.operatingCashFlow) || !js.isUndefined(q.leveredFreeCashFlow)
  }

  private def isShareStatisticsVisible(q: js.Dynamic) = {
    !js.isUndefined(q.avgVolume3Month) || !js.isUndefined(q.avgVolume10Day) || !js.isUndefined(q.sharesOutstanding) ||
      !js.isUndefined(q.sharesFloat) || !js.isUndefined(q.pctHeldByInsiders) || !js.isUndefined(q.pctHeldByInstitutions) ||
      !js.isUndefined(q.sharesShort) || !js.isUndefined(q.shortRatio) || !js.isUndefined(q.shortPctOfFloat) ||
      !js.isUndefined(q.sharesShortPriorMonth)
  }

  private def isDividendsSplitsVisible(q: js.Dynamic) = {
    !js.isUndefined(q.forwardAnnualDividendRate) || !js.isUndefined(q.forwardAnnualDividendYield) ||
      !js.isUndefined(q.trailingAnnualDividendYield) || !js.isUndefined(q.divYield5YearAvg) || !js.isUndefined(q.payoutRatio) ||
      !js.isUndefined(q.dividendDate) || !js.isUndefined(q.exDividendDate) || !js.isUndefined(q.lastSplitFactor) ||
      !js.isUndefined(q.lastSplitDate)
  }

  private def onTransactionHistory(q: js.Dynamic, module: Expander) = {
    if ($scope.tradingHistory.isEmpty && q.assetType.asInstanceOf[String] == "Common Stock") {
      loadTradingHistory(q.symbol.asInstanceOf[String], module)
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // load the symbol
  if (!js.isUndefined($scope.quote.symbol)) {
    val params = $routeParams.asInstanceOf[js.Dynamic]
    // get the symbol
    val symbol = if (!js.isUndefined(params.symbol))
      params.symbol.asInstanceOf[String]
    else
      $cookieStore.get("symbol").toOption.getOrElse(recentSymbols.getLast)

    // load the symbol
    loadQuote(symbol)
  }

}

/**
 * Discover Controller
 * @author lawrence.daniels@gmail.com
 */
object DiscoverController {

  /**
   * Dashboard Scope
   * @author lawrence.daniels@gmail.com
   */
  trait DiscoverScope extends Scope {
    var marketClock = new js.Date().toTimeString()
    var ticker: String = js.native
    var quote: js.Dynamic = js.native
    var usMarketsOpen: Boolean = js.native
    var loading: Boolean = js.native

    def startLoading() = loading = true

    def stopLoading() = loading = false

    // define the display options
    val options = js.Dictionary[String]()

    // setup filtered quotes & trading history
    var tradingHistory = js.Array[js.Any]()
    var selectedTradingHistory: js.Any = js.native

  }

  case class Expander(title: String,
                      url: String,
                      icon: String,
                      var expanded: Boolean,
                      var loading: Boolean = false,
                      visible: js.Dynamic => Boolean,
                      onExpand: (js.Dynamic, Expander) => Unit = null)

  case class Ticker(symbol: String)

}