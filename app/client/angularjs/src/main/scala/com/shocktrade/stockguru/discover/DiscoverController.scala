package com.shocktrade.stockguru.discover

import com.shocktrade.common.models.Profile
import com.shocktrade.common.models.quote.{AutoCompleteQuote, CompleteQuote}
import com.shocktrade.stockguru._
import com.shocktrade.stockguru.dialogs.NewOrderDialog
import com.shocktrade.stockguru.dialogs.NewOrderDialogController.{NewOrderDialogResult, NewOrderParams}
import com.shocktrade.stockguru.discover.DiscoverController._
import com.shocktrade.stockguru.profile.ProfileService
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs.cookies.Cookies
import org.scalajs.angularjs.http.HttpResponse
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{angular, injected, _}
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success, Try}

/**
  * Discover Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
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
  $scope.q = CompleteQuote()

  // define the display options
  $scope.options = new DiscoverOptions(range = $cookies.getOrElse("chart_range", "5d"))

  // define the quote section expanders
  $scope.expanders = js.Array(
    new ModuleExpander(title = "Performance & Risk",
      url = "/views/discover/expanders/price_performance.html",
      icon = "fa-line-chart",
      visible = isPerformanceRisk),
    new ModuleExpander(title = "Income Statement",
      url = "/views/discover/expanders/income_statement.html",
      icon = "fa-money",
      visible = isIncomeStatement),
    new ModuleExpander(title = "Balance Sheet",
      url = "/views/discover/expanders/balance_sheet.html",
      icon = "fa-calculator",
      visible = isBalanceSheet),
    new ModuleExpander(title = "Valuation Measures",
      url = "/views/discover/expanders/valuation_measures.html",
      icon = "fa-gears",
      visible = isValuationMeasures),
    new ModuleExpander(title = "Share Statistics",
      url = "/views/discover/expanders/share_statistics.html",
      icon = "fa-bar-chart",
      visible = isShareStatistics),
    new ModuleExpander(title = "Dividends & Splits",
      url = "/views/discover/expanders/dividends_splits.html",
      icon = "fa-cut",
      visible = isDividendsSplits),
    new ModuleExpander(title = "Historical Quotes",
      url = "/views/discover/quotes/trading_history.html",
      icon = "fa-calendar")
  )

  ///////////////////////////////////////////////////////////////////////////
  //          Public Function
  ///////////////////////////////////////////////////////////////////////////

  $scope.expandSection = (aModule: js.UndefOr[ModuleExpander]) => aModule foreach { module =>
    module.expanded = !module.expanded
  }

  $scope.popupNewOrderDialog = (aSymbol: js.UndefOr[String]) => aSymbol map { symbol =>
    newOrderDialog.popup(new NewOrderParams(symbol = symbol))
  }

  $scope.loadQuote = (aValue: js.UndefOr[Any]) => aValue foreach {
    case ticker: String =>
      updateQuote(ticker.indexOf(" ") match {
        case -1 => ticker
        case index => ticker.substring(0, index)
      })

    case unknown =>
      Try(unknown.asInstanceOf[AutoCompleteQuote]) match {
        case Success(quote) =>
          console.log(s"Loading symbol from ${angular.toJson(quote, pretty = false)}")
          quote.symbol foreach updateQuote
        case Failure(e) =>
          console.error(s"Unhandled value '$unknown': ${e.displayMessage}")
      }
  }

  private def updateQuote(ticker: String) {
    // get the symbol (e.g. "AAPL - Apple Inc")
    val symbol = if (ticker.contains(" ")) ticker.substring(0, ticker.indexOf(" ")).trim else ticker

    // load the quote
    asyncLoading($scope)(quoteService.getCompleteQuote(symbol)) onComplete {
      case Success(quote) if quote.symbol.isAssigned =>
        $scope.$apply { () =>
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
        }
      case Success(quote) =>
        console.log(s"quote = ${angular.toJson(quote)}")
        toaster.warning(s"Symbol not found")

      case Failure(e) =>
        console.error(s"Failed to retrieve quote: ${e.getMessage}")
        toaster.error(s"Error loading quote $symbol")
    }
  }


  //////////////////////////////////////////////////////////////////////
  //              Type-Ahead Functions
  //////////////////////////////////////////////////////////////////////

  $scope.formatSearchResult = (aResult: js.UndefOr[AutoCompleteQuote]) => {
    for {
      result <- aResult.flat
      symbol <- result.symbol
    } yield symbol
  }

  $scope.onSelectedItem = (aItem: js.UndefOr[AutoCompleteQuote], aModel: js.UndefOr[AutoCompleteQuote], aLabel: js.UndefOr[String]) => {
    aModel.flatMap(_.symbol) foreach { symbol =>
      console.log(s"Loading '$symbol' => ${angular.toJson(aModel)}")
      $scope.loadQuote(symbol)
    }
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
              $scope.$apply(() => usMarketStatus = Left(status))

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
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object DiscoverController {
  val LastSymbolCookie = "QuoteService_lastSymbol"

  def isPerformanceRisk: js.Function1[js.UndefOr[CompleteQuote], Boolean] = (aQuote: js.UndefOr[CompleteQuote]) => aQuote.exists { q =>
    /*q.high52Week.nonEmpty || q.low52Week.nonEmpty || q.change52Week.nonEmpty ||
      q.movingAverage50Day.nonEmpty || q.movingAverage200Day.nonEmpty ||
      q.change52WeekSNP500.nonEmpty || q.beta.nonEmpty*/
    true
  }

  def isIncomeStatement: js.Function1[js.UndefOr[CompleteQuote], Boolean] = (aQuote: js.UndefOr[CompleteQuote]) => aQuote.exists { q =>
    /*q.revenue.nonEmpty || q.revenuePerShare.nonEmpty || q.revenueGrowthQuarterly.nonEmpty ||
      q.grossProfit.nonEmpty || q.EBITDA.nonEmpty || q.netIncomeAvailToCommon.nonEmpty ||
      q.dilutedEPS.nonEmpty || q.earningsGrowthQuarterly.nonEmpty*/
    true
  }

  def isBalanceSheet: js.Function1[js.UndefOr[CompleteQuote], Boolean] = (aQuote: js.UndefOr[CompleteQuote]) => aQuote.exists { q =>
    /*q.totalCash.nonEmpty || q.totalDebt.nonEmpty || q.currentRatio.nonEmpty ||
      q.totalCashPerShare.nonEmpty || q.totalDebtOverEquity.nonEmpty || q.bookValuePerShare.nonEmpty ||
      q.returnOnAssets.nonEmpty || q.profitMargin.nonEmpty || q.mostRecentQuarterDate.nonEmpty ||
      q.returnOnEquity.nonEmpty || q.operatingMargin.nonEmpty || q.fiscalYearEndDate.nonEmpty*/
    true
  }

  def isValuationMeasures: js.Function1[js.UndefOr[CompleteQuote], Boolean] = (aQuote: js.UndefOr[CompleteQuote]) => aQuote.exists { q =>
    /*q.enterpriseValue.nonEmpty || q.trailingPE.nonEmpty || q.forwardPE.nonEmpty ||
      q.pegRatio.nonEmpty || q.priceOverSales.nonEmpty || q.priceOverBookValue.nonEmpty ||
      q.enterpriseValueOverRevenue.nonEmpty || q.enterpriseValueOverEBITDA.nonEmpty ||
      q.operatingCashFlow.nonEmpty || q.leveredFreeCashFlow.nonEmpty*/
    true
  }

  def isShareStatistics: js.Function1[js.UndefOr[CompleteQuote], Boolean] = (aQuote: js.UndefOr[CompleteQuote]) => aQuote.exists { q =>
    /*q.avgVolume3Month.nonEmpty || q.avgVolume10Day.nonEmpty || q.sharesOutstanding.nonEmpty ||
      q.sharesFloat.nonEmpty || q.pctHeldByInsiders.nonEmpty || q.pctHeldByInstitutions.nonEmpty ||
      q.sharesShort.nonEmpty || q.shortRatio.nonEmpty || q.shortPctOfFloat.nonEmpty || q.sharesShortPriorMonth.nonEmpty*/
    true
  }

  def isDividendsSplits: js.Function1[js.UndefOr[CompleteQuote], Boolean] = (aQuote: js.UndefOr[CompleteQuote]) => aQuote.exists { q =>
    /*q.forwardAnnualDividendRate.nonEmpty || q.forwardAnnualDividendYield.nonEmpty ||
      q.trailingAnnualDividendYield.nonEmpty || q.divYield5YearAvg.nonEmpty ||
      q.payoutRatio.nonEmpty || q.dividendDate.nonEmpty || q.exDividendDate.nonEmpty ||
      q.lastSplitFactor.nonEmpty || q.lastSplitDate.nonEmpty*/
    true
  }

  /**
    * Module Expander
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @ScalaJSDefined
  class ModuleExpander(val title: String,
                       val url: String,
                       val icon: String,
                       var expanded: Boolean = false,
                       val visible: js.UndefOr[js.Function] = js.undefined) extends js.Object

}

/**
  * Discover Controller Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait DiscoverControllerScope extends AutoCompletionControllerScope {
  // variables
  var expanders: js.Array[ModuleExpander] = js.native
  var options: DiscoverOptions = js.native
  var q: CompleteQuote = js.native
  var ticker: String = js.native
  var tradingHistory: js.Array[js.Object] = js.native

  // functions
  var expandSection: js.Function1[js.UndefOr[ModuleExpander], Unit] = js.native
  var isUSMarketsOpen: js.Function0[js.UndefOr[Boolean]] = js.native
  var loadQuote: js.Function1[js.UndefOr[Any], Unit] = js.native
  var popupNewOrderDialog: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[NewOrderDialogResult]]] = js.native

  // type-ahead functions
  var formatSearchResult: js.Function1[js.UndefOr[AutoCompleteQuote], js.UndefOr[String]] = js.native
  var onSelectedItem: js.Function3[js.UndefOr[AutoCompleteQuote], js.UndefOr[AutoCompleteQuote], js.UndefOr[String], Unit] = js.native

  // favorite quote functions
  var addFavoriteSymbol: js.Function1[js.UndefOr[String], js.UndefOr[HttpResponse[Profile]]] = js.native
  var isFavorite: js.Function1[js.UndefOr[String], Boolean] = js.native
  var removeFavoriteSymbol: js.Function1[js.UndefOr[String], js.UndefOr[HttpResponse[Profile]]] = js.native

  // recently-viewed quote functions
  var addRecentSymbol: js.Function1[js.UndefOr[String], js.UndefOr[HttpResponse[Profile]]] = js.native
  var isRecentSymbol: js.Function1[js.UndefOr[String], Boolean] = js.native
  var removeRecentSymbol: js.Function1[js.UndefOr[String], js.UndefOr[HttpResponse[Profile]]] = js.native

  // risk functions
  var getBetaClass: js.Function1[js.UndefOr[Double], js.UndefOr[Object]] = js.native
  var getRiskClass: js.Function1[js.UndefOr[String], js.UndefOr[Object]] = js.native
  var getRiskDescription: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native

}

/**
  * Discover Options
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class DiscoverOptions(var range: String) extends js.Object

/**
  * Discover Route Parameters
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait DiscoverRouteParams extends js.Object {
  var symbol: js.UndefOr[String] = js.native

}
