package com.shocktrade.client.discover

import com.shocktrade.client._
import com.shocktrade.client.contest.PortfolioService
import com.shocktrade.client.dialogs.NewOrderDialog
import com.shocktrade.client.dialogs.NewOrderDialogController.{NewOrderDialogResult, NewOrderParams}
import com.shocktrade.client.discover.DiscoverController._
import com.shocktrade.client.users.{PersonalSymbolSupport, PersonalSymbolSupportScope, UserService}
import com.shocktrade.common.models.quote.{AutoCompleteQuote, CompleteQuote}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{angular, injected, _}
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
 * Discover Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class DiscoverController($scope: DiscoverControllerScope, $cookies: Cookies, $location: Location, $q: Q,
                              $routeParams: DiscoverRouteParams, $timeout: Timeout, toaster: Toaster,
                              @injected("MarketStatusService") marketStatusService: MarketStatusService,
                              @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
                              @injected("PortfolioService") portfolioService: PortfolioService,
                              @injected("QuoteService") quoteService: QuoteService,
                              @injected("UserService") userService: UserService)
  extends AutoCompletionController($scope, $q, quoteService)
    with GlobalLoading
    with GlobalSelectedSymbol
    with PersonalSymbolSupport
    with USMarketsStatusSupport {

  // setup the public variables
  $scope.ticker = $routeParams.symbol.orNull
  $scope.q = CompleteQuote()

  // define the display options
  $scope.options = new DiscoverOptions(range = $cookies.getOrElse("chart_range", "5d"))

  // define the quote section expanders
  $scope.expanders = js.Array(
    new ModuleExpander(title = "Historical Quotes",
      url = "/views/discover/quotes/trading_history.html",
      icon = "fa-calendar"),
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
      visible = isDividendsSplits)
  )

  ///////////////////////////////////////////////////////////////////////////
  //          Public Function
  ///////////////////////////////////////////////////////////////////////////

  $scope.expandSection = (aModule: js.UndefOr[ModuleExpander]) => aModule foreach { module =>
    module.expanded = !module.expanded
  }

  $scope.popupNewOrderDialog = (aContestID: js.UndefOr[String], aUserID: js.UndefOr[String], aSymbol: js.UndefOr[String]) => {
    for {
      contestID <- aContestID
      userID <- aUserID
      symbol <- aSymbol
    } yield newOrderDialog.popup(new NewOrderParams(contestID = contestID, userID = userID, symbol = symbol))
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

  override def onSymbolSelected(newSymbol: String, oldSymbol: Option[String]): Unit = {
    console.log(s"The selected symbol has changed to '$newSymbol'")
    updateQuote(newSymbol)
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
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  // auto-load the passed symbol
  $routeParams.symbol.foreach { symbol =>
    console.info(s"Auto-loading symbol '$symbol'...")
    $scope.selectedSymbol = symbol
    updateQuote(symbol)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  private def updateQuote(ticker: String) {
    // get the symbol (e.g. "AAPL - Apple Inc")
    val symbol = if (ticker.contains(" ")) ticker.substring(0, ticker.indexOf(" ")).trim else ticker

    // load the quote
    asyncLoading($scope)(quoteService.getCompleteQuote(symbol)) onComplete {
      case Success(response) if response.data.symbol.isAssigned =>
        val quote = response.data
        $scope.$apply { () =>
          // capture the quote
          $scope.q = quote
          $scope.ticker = s"${quote.symbol} - ${quote.name}"

          // update the address bar
          $location.search("symbol", quote.symbol)

          // add the symbol to the Recently-viewed Symbols
          $scope.userProfile.flatMap(_.userID) foreach { userID => userService.addRecentSymbol(userID, symbol) }

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

  // setup the chart range
  $scope.$watch("options.range", (newValue: js.UndefOr[Any], oldValue: js.Any) => newValue.foreach($cookies.put("chart_range", _)))

}

/**
 * Discover Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object DiscoverController {

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
   * Discover Route Parameters
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait DiscoverRouteParams extends js.Object {
    var symbol: js.UndefOr[String] = js.native
  }

  /**
   * Module Expander
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
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
trait DiscoverControllerScope extends RootScope
  with AutoCompletionControllerScope
  with GlobalSelectedSymbolScope
  with PersonalSymbolSupportScope
  with USMarketsStatusSupportScope {

  // variables
  var expanders: js.Array[ModuleExpander] = js.native
  var options: DiscoverOptions = js.native
  var q: CompleteQuote = js.native
  var ticker: String = js.native
  var tradingHistory: js.Array[js.Object] = js.native
  //var userProfile: js.UndefOr[UserProfile] = js.native

  // functions
  var expandSection: js.Function1[js.UndefOr[ModuleExpander], Unit] = js.native
  var loadQuote: js.Function1[js.UndefOr[Any], Unit] = js.native
  var popupNewOrderDialog: js.Function3[js.UndefOr[String], js.UndefOr[String], js.UndefOr[String], js.UndefOr[js.Promise[NewOrderDialogResult]]] = js.native

  // type-ahead functions
  var formatSearchResult: js.Function1[js.UndefOr[AutoCompleteQuote], js.UndefOr[String]] = js.native
  var onSelectedItem: js.Function3[js.UndefOr[AutoCompleteQuote], js.UndefOr[AutoCompleteQuote], js.UndefOr[String], Unit] = js.native

  // risk functions
  var getBetaClass: js.Function1[js.UndefOr[Double], js.UndefOr[Object]] = js.native
  var getRiskClass: js.Function1[js.UndefOr[String], js.UndefOr[Object]] = js.native
  var getRiskDescription: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native

}

/**
 * Discover Options
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class DiscoverOptions(var range: String) extends js.Object
