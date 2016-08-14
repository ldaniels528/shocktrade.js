package com.shocktrade.javascript.discover

import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.cookies.Cookies
import org.scalajs.angularjs.http.HttpResponse
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.angularjs.{angular, injected}
import com.shocktrade.javascript._
import com.shocktrade.javascript.dialogs.NewOrderDialogController.NewOrderDialogResult
import com.shocktrade.javascript.dialogs.{NewOrderDialog, NewOrderParams}
import com.shocktrade.javascript.discover.DiscoverController._
import com.shocktrade.javascript.models.{FullQuote, OrderQuote}
import com.shocktrade.javascript.profile.ProfileService
import org.scalajs.dom.console

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Discover Controller
  * @author lawrence.daniels@gmail.com
  */
class DiscoverController($scope: DiscoverControllerScope, $cookies: Cookies, $location: Location, $q: Q,
                         $routeParams: DiscoverRouteParams, $timeout: Timeout, toaster: Toaster,
                         @injected("MarketStatus") marketStatus: MarketStatusService,
                         @injected("MySessionService") mySession: MySessionService,
                         @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
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

  $scope.expandSection = (aModule: js.UndefOr[ModuleExpander]) => aModule foreach { module =>
    module.expanded = !module.expanded
  }

  $scope.popupNewOrderDialog = (aSymbol: js.UndefOr[String]) => aSymbol map { symbol =>
    newOrderDialog.popup(NewOrderParams(symbol = symbol))
  }

  $scope.loadQuote = (ticker: js.Dynamic) => {
    console.log(s"Loading symbol ${angular.toJson(ticker, pretty = false)}")

    // determine the symbol
    val symbol = (if (isDefined(ticker.symbol)) ticker.symbol.asOpt[String]
    else {
      ticker.asOpt[String] map { _ticker =>
        val index = _ticker.indexOf(" ")
        if (index == -1) _ticker else _ticker.substring(0, index)
      }
    }) map(_.toUpperCase)

    symbol foreach updateQuote
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
          $scope.expandSection($scope.expanders(6))
        }

      case Success(quote) =>
        console.log(s"quote = ${angular.toJson(quote)}")
        toaster.warning(s"Symbol not found")

      case Failure(e) =>
        console.error(s"Failed to retrieve quote: ${e.getMessage}")
        toaster.error(s"Error loading quote $symbol")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          ETF Holdings / Products
  ///////////////////////////////////////////////////////////////////////////

  $scope.hasHoldings = (aQuote: js.UndefOr[FullQuote]) => aQuote exists { q =>
    q.legalType.exists(_ == "ETF") && q.products.exists(_.nonEmpty)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Symbols - Favorites
  ///////////////////////////////////////////////////////////////////////////

  $scope.addFavoriteSymbol = (aSymbol: js.UndefOr[String]) => {
    for {
      symbol <- aSymbol
      userId <- mySession.userProfile._id
    } yield profileService.addFavoriteSymbol(userId, symbol)
  }

  $scope.isFavorite = (aSymbol: js.UndefOr[String]) => aSymbol.exists(mySession.isFavoriteSymbol)

  $scope.removeFavoriteSymbol = (aSymbol: js.UndefOr[String]) => {
    for {
      symbol <- aSymbol
      userId <- mySession.userProfile._id
    } yield profileService.removeFavoriteSymbol(userId, symbol)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Symbols - Recently Viewed
  ///////////////////////////////////////////////////////////////////////////

  $scope.addRecentSymbol = (aSymbol: js.UndefOr[String]) => {
    for {
      symbol <- aSymbol
      userId <- mySession.userProfile._id
    } yield profileService.addRecentSymbol(userId, symbol)
  }

  $scope.isRecentSymbol = (symbol: js.UndefOr[String]) => {
    symbol.exists(mySession.isRecentSymbol)
  }

  $scope.removeRecentSymbol = (aSymbol: js.UndefOr[String]) => {
    for {
      symbol <- aSymbol
      userId <- mySession.userProfile._id
    } yield profileService.removeRecentSymbol(userId, symbol)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Risk Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getBetaClass = (beta: js.UndefOr[Double]) => beta map {
    case b if b > 1.3 || b < -1.3 => "volatile_red"
    case b if b >= 0.0 => "volatile_green"
    case b if b < 0 => "volatile_yellow"
    case _ => js.undefined
  }

  $scope.getRiskClass = (riskLevel: js.UndefOr[String]) => riskLevel map {
    case rs if isDefined(rs) && rs.nonEmpty => s"risk_${rs.toLowerCase}"
    case _ => js.undefined
  }

  $scope.getRiskDescription = (riskLevel: js.UndefOr[String]) => riskLevel map {
    case "Low" => "Generally recommended for investment"
    case "Medium" => "Not recommended for inexperienced investors"
    case "High" => "Not recommended for investment"
    case "Unknown" => "The risk level could not be determined"
    case _ => "The risk level could not be determined"
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Market Status Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isUSMarketsOpen = () => {
    usMarketStatus match {
      case Left(status) => Option(status.active).orUndefined
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
              console.error(s"Failed to retrieve market status: ${e.getMessage}")
          }
        }
        js.undefined
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // load the symbol
  if ($scope.q.symbol.isEmpty) {
    // get the symbol
    val symbol = $routeParams.symbol getOrElse $cookies.getOrElse(LastSymbolCookie, mySession.getMostRecentSymbol)

    // load the symbol
    updateQuote(symbol)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  // setup the chart range
  $scope.$watch("options.range", (newValue: js.UndefOr[Any], oldValue: js.Any) => newValue.foreach($cookies.put("chart_range", _)))

}

/**
  * Discover Controller
  */
object DiscoverController {
  val LastSymbolCookie = "QuoteService_lastSymbol"

  def isPerformanceRisk: js.Function1[js.UndefOr[FullQuote], Boolean] = (aQuote: js.UndefOr[FullQuote]) => aQuote.exists { q =>
    q.high52Week.nonEmpty || q.low52Week.nonEmpty || q.change52Week.nonEmpty ||
      q.movingAverage50Day.nonEmpty || q.movingAverage200Day.nonEmpty ||
      q.change52WeekSNP500.nonEmpty || q.beta.nonEmpty
  }

  def isIncomeStatement: js.Function1[js.UndefOr[FullQuote], Boolean] = (aQuote: js.UndefOr[FullQuote]) => aQuote.exists { q =>
    q.revenue.nonEmpty || q.revenuePerShare.nonEmpty || q.revenueGrowthQuarterly.nonEmpty ||
      q.grossProfit.nonEmpty || q.EBITDA.nonEmpty || q.netIncomeAvailToCommon.nonEmpty ||
      q.dilutedEPS.nonEmpty || q.earningsGrowthQuarterly.nonEmpty
  }

  def isBalanceSheet: js.Function1[js.UndefOr[FullQuote], Boolean] = (aQuote: js.UndefOr[FullQuote]) => aQuote.exists { q =>
    q.totalCash.nonEmpty || q.totalDebt.nonEmpty || q.currentRatio.nonEmpty ||
      q.totalCashPerShare.nonEmpty || q.totalDebtOverEquity.nonEmpty || q.bookValuePerShare.nonEmpty ||
      q.returnOnAssets.nonEmpty || q.profitMargin.nonEmpty || q.mostRecentQuarterDate.nonEmpty ||
      q.returnOnEquity.nonEmpty || q.operatingMargin.nonEmpty || q.fiscalYearEndDate.nonEmpty
  }

  def isValuationMeasures: js.Function1[js.UndefOr[FullQuote], Boolean] = (aQuote: js.UndefOr[FullQuote]) => aQuote.exists { q =>
    q.enterpriseValue.nonEmpty || q.trailingPE.nonEmpty || q.forwardPE.nonEmpty ||
      q.pegRatio.nonEmpty || q.priceOverSales.nonEmpty || q.priceOverBookValue.nonEmpty ||
      q.enterpriseValueOverRevenue.nonEmpty || q.enterpriseValueOverEBITDA.nonEmpty ||
      q.operatingCashFlow.nonEmpty || q.leveredFreeCashFlow.nonEmpty
  }

  def isShareStatistics: js.Function1[js.UndefOr[FullQuote], Boolean] = (aQuote: js.UndefOr[FullQuote]) => aQuote.exists { q =>
    q.avgVolume3Month.nonEmpty || q.avgVolume10Day.nonEmpty || q.sharesOutstanding.nonEmpty ||
      q.sharesFloat.nonEmpty || q.pctHeldByInsiders.nonEmpty || q.pctHeldByInstitutions.nonEmpty ||
      q.sharesShort.nonEmpty || q.shortRatio.nonEmpty || q.shortPctOfFloat.nonEmpty || q.sharesShortPriorMonth.nonEmpty
  }

  def isDividendsSplits: js.Function1[js.UndefOr[FullQuote], Boolean] = (aQuote: js.UndefOr[FullQuote]) => aQuote.exists { q =>
    q.forwardAnnualDividendRate.nonEmpty || q.forwardAnnualDividendYield.nonEmpty ||
      q.trailingAnnualDividendYield.nonEmpty || q.divYield5YearAvg.nonEmpty ||
      q.payoutRatio.nonEmpty || q.dividendDate.nonEmpty || q.exDividendDate.nonEmpty ||
      q.lastSplitFactor.nonEmpty || q.lastSplitDate.nonEmpty
  }

  // define the Quote module expanders
  val Expanders = js.Array(
    ModuleExpander(title = "Performance & Risk",
      url = "/assets/views/discover/expanders/price_performance.htm",
      icon = "fa-line-chart",
      expanded = false,
      visible = isPerformanceRisk),
    ModuleExpander(title = "Income Statement",
      url = "/assets/views/discover/expanders/income_statement.htm",
      icon = "fa-money",
      expanded = false,
      visible = isIncomeStatement),
    ModuleExpander(title = "Balance Sheet",
      url = "/assets/views/discover/expanders/balance_sheet.htm",
      icon = "fa-calculator",
      expanded = false,
      visible = isBalanceSheet),
    ModuleExpander(title = "Valuation Measures",
      url = "/assets/views/discover/expanders/valuation_measures.htm",
      icon = "fa-gears",
      expanded = false,
      visible = isValuationMeasures),
    ModuleExpander(title = "Share Statistics",
      url = "/assets/views/discover/expanders/share_statistics.htm",
      icon = "fa-bar-chart",
      expanded = false,
      visible = isShareStatistics),
    ModuleExpander(title = "Dividends & Splits",
      url = "/assets/views/discover/expanders/dividends_splits.htm",
      icon = "fa-cut",
      expanded = false,
      visible = isDividendsSplits),
    ModuleExpander(title = "Historical Quotes",
      url = "/assets/views/discover/quotes/trading_history.htm",
      icon = "fa-calendar",
      expanded = false))

}

/**
  * Discover Controller Scope
  */
@js.native
trait DiscoverControllerScope extends AutoCompletionControllerScope {
  // variables
  var expanders: js.Array[ModuleExpander]
  var options: DiscoverOptions
  var q: OrderQuote
  var ticker: String
  var tradingHistory: js.Array[js.Object]

  // functions
  var expandSection: js.Function1[js.UndefOr[ModuleExpander], Unit]
  var hasHoldings: js.Function1[js.UndefOr[FullQuote], Boolean]
  var isUSMarketsOpen: js.Function0[js.UndefOr[Boolean]]
  var loadQuote: js.Function1[js.Dynamic, Unit]
  var popupNewOrderDialog: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[NewOrderDialogResult]]]

  // favorite quote functions
  var addFavoriteSymbol: js.Function1[js.UndefOr[String], js.UndefOr[HttpResponse[js.Dynamic]]]
  var isFavorite: js.Function1[js.UndefOr[String], Boolean]
  var removeFavoriteSymbol: js.Function1[js.UndefOr[String], js.UndefOr[HttpResponse[js.Dynamic]]]

  // recently-viewed quote functions
  var addRecentSymbol: js.Function1[js.UndefOr[String], js.UndefOr[HttpResponse[js.Dynamic]]]
  var isRecentSymbol: js.Function1[js.UndefOr[String], Boolean]
  var removeRecentSymbol: js.Function1[js.UndefOr[String], js.UndefOr[HttpResponse[js.Dynamic]]]

  // risk functions
  var getBetaClass: js.Function1[js.UndefOr[Double], js.UndefOr[Object]]
  var getRiskClass: js.Function1[js.UndefOr[String], js.UndefOr[Object]]
  var getRiskDescription: js.Function1[js.UndefOr[String], js.UndefOr[String]]

}

/**
  * Discover Options
  */
@js.native
trait DiscoverOptions extends js.Object {
  var range: String
}

/**
  * Discover Options Singleton
  */
object DiscoverOptions {

  def apply(range: String) = {
    val options = New[DiscoverOptions]
    options.range = range
    options
  }

}

/**
  * Discover Route Parameters
  */
@js.native
trait DiscoverRouteParams extends js.Object {
  var symbol: js.UndefOr[String]

}
