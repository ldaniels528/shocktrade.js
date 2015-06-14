package com.shocktrade.javascript.discover

import biz.enef.angulate.core.{Location, Timeout}
import biz.enef.angulate.{ScopeController, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.profile.ProfileService

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Failure, Success}

/**
 * Favorite Symbols Service
 * @author lawrence.daniels@gmail.com
 */
class FavoritesController($scope: js.Dynamic, $location: Location, $routeParams: js.Dynamic, toaster: Toaster,
                          @named("MySession") mySession: MySession,
                          @named("ProfileService") profileService: ProfileService,
                          @named("QuoteService") quoteService: QuoteService)
  extends ScopeController {

  private var quotes = emptyArray[js.Dynamic]
  $scope.selectedQuote = null

  $scope.cancelSelection = () => $scope.selectedQuote = null

  $scope.isSplitScreen = () => isDefined($scope.selectedQuote)

  $scope.selectQuote = (quote: js.Dynamic) => {
    $location.search("symbol", quote.symbol)
    $scope.selectedQuote = quote
  }

  /////////////////////////////////////////////////////////////////////////////
  //			C.R.U.D. Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.getFavoriteQuotes = () => quotes

  $scope.getQuoteCount = () => quotes.length

  $scope.addFavoriteSymbol = (symbol: String) => {
    profileService.addFavoriteSymbol(mySession.getUserID(), symbol) onComplete {
      case Success(response) =>
      case Failure(e) =>
        toaster.error("Failed to add favorite symbol")
        g.console.error(s"Failed to add favorite symbol: ${e.getMessage}")
    }
  }

  $scope.isFavorite = (symbol: String) => mySession.isFavoriteSymbol(symbol)

  $scope.removeFavoriteSymbol = (symbol: String) => {
    profileService.removeFavoriteSymbol(mySession.getUserID(), symbol) onComplete {
      case Success(response) =>
      case Failure(e) =>
        toaster.error("Failed to remove favorite symbol")
        g.console.error(s"Failed to remove favorite symbol: ${e.getMessage}")
    }
  }

  private def loadQuotes(symbols: js.Array[String]) {
    g.console.log(s"Loading symbols ${symbols}...")
    quoteService.getStockQuoteList(symbols) onComplete {
      case Success(updatedQuotes) => quotes = updatedQuotes
      case Failure(e) =>
        toaster.error(s"Failed to load quote: ${e.getMessage}")
        g.console.error(s"Failed to load quote: ${e.getMessage}")
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Initialization
  /////////////////////////////////////////////////////////////////////////////

  if (isDefined($routeParams.symbol)) {
    val symbol = $routeParams.symbol.as[String]
    quotes.find(_.symbol === symbol) foreach { quote =>
      $scope.selectQuote(quote)
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Event Listeners
  /////////////////////////////////////////////////////////////////////////////

  $scope.$watch(mySession.getUserID, (newID: String, oldID: String) => {
    g.console.log(s"newID = $newID, oldID = $oldID")
    if (mySession.getFavoriteSymbols().nonEmpty) loadQuotes(mySession.getFavoriteSymbols())
  })

}
