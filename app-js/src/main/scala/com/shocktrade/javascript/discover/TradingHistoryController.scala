package com.shocktrade.javascript.discover

import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.github.ldaniels528.scalascript.{Controller, Scope, injected}
import com.shocktrade.javascript.models.HistoricalQuote
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Trading History Controller
  * @author lawrence.daniels@gmail.com
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

  $scope.selectTradingHistory = (aQuote: js.UndefOr[HistoricalQuote]) => {
    selectedTradingHistory = aQuote
  }

  $scope.loadTradingHistory = (aSymbol: js.UndefOr[String]) => aSymbol.toOption match {
    case Some(symbol) =>
      quoteService.getTradingHistory(symbol) onComplete {
        case Success(results) => tradingHistory = results
        case Failure(e) =>
          toaster.error(s"Error loading trading history for symbol '$symbol'")
          console.error(s"Error loading trading history for symbol '$symbol': ${e.getMessage}")
      }
    case None =>
      tradingHistory = emptyArray
      selectedTradingHistory = js.undefined
  }

}

/**
  * Trading History Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait TradingHistoryControllerScope extends Scope {
  // functions
  var getTradingHistory: js.Function0[js.Array[HistoricalQuote]]
  var getSelectedTradingHistory: js.Function0[js.UndefOr[HistoricalQuote]]
  var hasSelectedTradingHistory: js.Function0[Boolean]
  var isSelectedTradingHistory: js.Function1[js.UndefOr[HistoricalQuote], Boolean]
  var selectTradingHistory: js.Function1[js.UndefOr[HistoricalQuote], Unit]
  var loadTradingHistory: js.Function1[js.UndefOr[String], Unit]

}