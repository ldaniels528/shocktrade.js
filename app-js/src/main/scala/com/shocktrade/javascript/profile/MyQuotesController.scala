package com.shocktrade.javascript.profile

import biz.enef.angulate.core.Location
import biz.enef.angulate.{Scope, ScopeController, named}
import com.ldaniels528.javascript.angularjs.extensions.Toaster
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.discover.QuoteService
import com.shocktrade.javascript.profile.MyQuotesController._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * My Symbols Controller
 * @author lawrence.daniels@gmail.com
 */
class MyQuotesController($scope: js.Dynamic, $location: Location, $routeParams: js.Dynamic, toaster: Toaster,
                         @named("ContestService") contestService: ContestService,
                         @named("MySession") mySession: MySession,
                         @named("ProfileService") profileService: ProfileService,
                         @named("QuoteService") quoteService: QuoteService)
  extends ScopeController {
  private val quoteSets = js.Dictionary[js.Dynamic](QuoteLists map { case (name, icon) =>
    name -> JS(
      icon = icon,
      quotes = null,
      expanded = false,
      loading = false)
  }: _*)

  $scope.selectedQuote = null

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.isSelected = (quote: js.Dynamic) => $scope.selectedQuote == quote

  $scope.selectQuote = (quote: js.Dynamic) => $scope.selectedQuote = quote

  $scope.expandList = (name: String) => expandList(name)

  $scope.getQuoteSets = () => quoteSets

  $scope.addFavoriteSymbol = (symbol: String) => addFavoriteSymbol(symbol)

  $scope.isFavorite = (symbol: String) => mySession.isFavoriteSymbol(symbol)

  $scope.removeFavoriteSymbol = (symbol: String) => removeFavoriteSymbol(symbol)

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def addFavoriteSymbol(symbol: String) = {
    profileService.addFavoriteSymbol(mySession.getUserID(), symbol) onComplete {
      case Success(response) =>
      case Failure(e) =>
        toaster.error("Failed to add favorite symbol")
        g.console.error(s"Failed to add favorite symbol: ${e.getMessage}")
    }
  }

  private def expandList(name: String) {
    quoteSets.get(name) foreach { obj =>
      obj.expanded = !obj.expanded
      if (obj.expanded.isTrue && !isDefined(obj.quotes)) {
        obj.quotes = emptyArray[js.Dynamic]
        name match {
          case Favorites => loadQuotes(name, mySession.getFavoriteSymbols(), obj)
          case Held => loadHeldSecurities(obj)
          case Recents => loadQuotes(name, mySession.getRecentSymbols(), obj)
          case _ =>
            g.console.error(s"$name is not a recognized list")
        }
      }
    }
  }

  private def loadQuotes(name: String, symbols: js.Array[String], obj: js.Dynamic) {
    if (symbols.nonEmpty) {
      g.console.log(s"Loading $name: ${symbols.toSeq}...")
      quoteService.getStockQuoteList(symbols) onComplete {
        case Success(updatedQuotes) => obj.quotes = updatedQuotes
        case Failure(e) =>
          toaster.error(s"Failed to load quote: ${e.getMessage}")
          g.console.error(s"Failed to load quote: ${e.getMessage}")
      }
    }
  }

  private def loadHeldSecurities(obj: js.Dynamic): Unit = {
    mySession.userProfile.OID_? foreach { playerId =>
     val outcome = for {
        symbols <- contestService.getHeldSecurities(playerId)
        quotes <- quoteService.getStockQuoteList(symbols)
      } yield quotes

      outcome onComplete {
        case Success(updatedQuotes) =>
          g.console.log(s"updatedQuotes = ${toJson(updatedQuotes)}")
          obj.quotes = updatedQuotes
        case Failure(e) =>
          toaster.error(s"Failed to load quote: ${e.getMessage}")
          g.console.error(s"Failed to load quote: ${e.getMessage}")
      }
    }
  }

  private def reloadQuotes() {
    g.console.log("Updating Favorite Symbols...")
    /*
    if (mySession.getFavoriteSymbols().nonEmpty) loadQuotes(Favorites, mySession.getFavoriteSymbols())
    if (mySession.getRecentSymbols().nonEmpty) loadQuotes(Recents, mySession.getFavoriteSymbols())
    */
  }

  private def removeFavoriteSymbol(symbol: String) = {
    profileService.removeFavoriteSymbol(mySession.getUserID(), symbol) onComplete {
      case Success(response) =>
      case Failure(e) =>
        toaster.error("Failed to remove favorite symbol")
        g.console.error(s"Failed to remove favorite symbol: ${e.getMessage}")
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Event Listeners
  /////////////////////////////////////////////////////////////////////////////

  private val scope = $scope.asInstanceOf[Scope]

  /**
   * Listen for changes to the player's profile
   */
  scope.$on(UserProfileChanged, (profile: js.Dynamic) => reloadQuotes())

}

/**
 * My Symbols Controller
 * @author lawrence.daniels@gmail.com
 */
object MyQuotesController {
  private val Favorites = "Favorites"
  private val Recents = "Recents"
  private val Held = "Held Securities"

  private val QuoteLists = js.Array(
    Favorites -> "fa-heart",
    Recents -> "fa-history",
    Held -> "fa-star"
  )

}