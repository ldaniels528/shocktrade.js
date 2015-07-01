package com.shocktrade.javascript.discover

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.{ScalaJsHelper, Controller}
import com.ldaniels528.javascript.angularjs.extensions.Toaster
import ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Failure, Success}

/**
 * Trading History Controller
 * @author lawrence.daniels@gmail.com
 */
class TradingHistoryController($scope: js.Dynamic, toaster: Toaster, @named("QuoteService") quoteService: QuoteService)
  extends Controller {

  private var tradingHistory: js.Array[js.Dynamic] = null
  private var selectedTradingHistory: js.Dynamic = null

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getTradingHistory = () => tradingHistory

  $scope.getSelectedTradingHistory = () => selectedTradingHistory

  $scope.hasSelectedTradingHistory = () => isDefined(selectedTradingHistory)

  $scope.isSelectedTradingHistory = (t: js.Dynamic) => selectedTradingHistory == t

  $scope.selectTradingHistory = (t: js.Dynamic) => selectedTradingHistory = t

  $scope.loadTradingHistory = (symbol: js.UndefOr[String]) => loadTradingHistory(symbol)

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def loadTradingHistory(symbol_? : js.UndefOr[String]) = {
    symbol_?.foreach { symbol =>
      quoteService.getTradingHistory(symbol) onComplete {
        case Success(results) => tradingHistory = results
        case Failure(e) =>
          toaster.error(s"Error loading trading history for symbol '$symbol'")
          g.console.error(s"Error loading trading history for symbol '$symbol': ${e.getMessage}")
      }
    }
  }

}
