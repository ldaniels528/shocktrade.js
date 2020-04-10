package com.shocktrade.client.discover

import com.shocktrade.client.RootScope
import com.shocktrade.common.models.quote.HistoricalQuote
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, injected}
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Trading History Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class TradingHistoryController($scope: TradingHistoryControllerScope, toaster: Toaster,
                               @injected("QuoteService") quoteService: QuoteService)
  extends Controller {

  private var tradingHistory: js.Array[HistoricalQuote] = emptyArray
  private var selectedTradingHistory: js.UndefOr[HistoricalQuote] = js.undefined

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getTradingHistory = () => tradingHistory

  $scope.getSelectedTradingHistory = () => selectedTradingHistory

  $scope.hasSelectedTradingHistory = () => selectedTradingHistory.nonEmpty

  $scope.isSelectedTradingHistory = (aQuote: js.UndefOr[HistoricalQuote]) => {
    selectedTradingHistory.exists(t => aQuote.exists(_ == t))
  }

  $scope.selectTradingHistory = (aQuote: js.UndefOr[HistoricalQuote]) => selectedTradingHistory = aQuote

  $scope.loadTradingHistory = (aSymbol: js.UndefOr[String]) => loadTradingHistory(aSymbol)

  private def loadTradingHistory(aSymbol: js.UndefOr[String]): js.Promise[HttpResponse[js.Array[HistoricalQuote]]] = aSymbol.toOption match {
    case Some(symbol) =>
      val outcome = quoteService.getTradingHistory(symbol)
      outcome onComplete {
        case Success(results) => $scope.$apply(() => tradingHistory = results.data)
        case Failure(e) =>
          toaster.error(s"Error loading trading history for symbol '$symbol'")
          console.error(s"Error loading trading history for symbol '$symbol': ${e.getMessage}")
      }
      outcome
    case None =>
      tradingHistory = emptyArray
      selectedTradingHistory = js.undefined
      js.Promise.reject("No symbol found")
  }

}

/**
 * Trading History Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait TradingHistoryControllerScope extends RootScope {
  // functions
  var getTradingHistory: js.Function0[js.Array[HistoricalQuote]]
  var getSelectedTradingHistory: js.Function0[js.UndefOr[HistoricalQuote]]
  var hasSelectedTradingHistory: js.Function0[Boolean]
  var isSelectedTradingHistory: js.Function1[js.UndefOr[HistoricalQuote], Boolean]
  var selectTradingHistory: js.Function1[js.UndefOr[HistoricalQuote], Unit]
  var loadTradingHistory: js.Function1[js.UndefOr[String], js.Promise[HttpResponse[js.Array[HistoricalQuote]]]]

}