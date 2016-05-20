package com.shocktrade.javascript.profile

import com.github.ldaniels528.meansjs.angularjs._
import com.github.ldaniels528.meansjs.angularjs.Location
import com.github.ldaniels528.meansjs.angularjs.toaster.Toaster
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.MySessionService
import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.discover.{BasicQuote, QuoteService}
import com.shocktrade.javascript.models.UserProfile
import com.shocktrade.javascript.profile.MyQuotesController._
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
 * My Symbols Controller
 * @author lawrence.daniels@gmail.com
 */
class MyQuotesController($scope: js.Dynamic, $location: Location, toaster: Toaster,
                         @injected("ContestService") contestService: ContestService,
                         @injected("MySessionService") mySession: MySessionService,
                         @injected("ProfileService") profileService: ProfileService,
                         @injected("QuoteService") quoteService: QuoteService)
  extends Controller {

  private val quoteSets = js.Dictionary(QuoteLists map { case (name, icon) =>
    name -> new Expandable(
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
    mySession.userProfile._id foreach { userId =>
      profileService.addFavoriteSymbol(userId, symbol) onComplete {
        case Success(response) =>
        case Failure(e) =>
          toaster.error("Failed to add favorite symbol")
          console.error(s"Failed to add favorite symbol: ${e.getMessage}")
      }
    }
  }

  private def expandList(name: String) {
    quoteSets.get(name) foreach { obj =>
      obj.expanded = !obj.expanded
      if (obj.expanded && !isDefined(obj.quotes)) {
        obj.quotes = emptyArray[BasicQuote]
        name match {
          case Favorites => loadQuotes(name, mySession.getFavoriteSymbols, obj)
          case Held => loadHeldSecurities(obj)
          case Recents => loadQuotes(name, mySession.getRecentSymbols, obj)
          case _ =>
            console.error(s"$name is not a recognized list")
        }
      }
    }
  }

  private def loadQuotes(name: String, symbols: js.Array[String], obj: Expandable) {
    if (symbols.nonEmpty) {
      quoteService.getStockQuoteList(symbols) onComplete {
        case Success(updatedQuotes) => obj.quotes = updatedQuotes
        case Failure(e) =>
          toaster.error(s"Failed to load $name")
          console.error(s"Failed to load $name: ${e.getMessage}")
      }
    }
  }

  private def loadHeldSecurities(obj: Expandable): Unit = {
    mySession.userProfile._id foreach { playerId =>
      val outcome = for {
        symbols <- contestService.getHeldSecurities(playerId)
        quotes <- quoteService.getStockQuoteList(symbols)
      } yield quotes

      outcome onComplete {
        case Success(updatedQuotes) =>
          obj.quotes = updatedQuotes
        case Failure(e) =>
          toaster.error("Failed to load Held Securities")
          console.error(s"Failed to load Held Securities: ${e.getMessage}")
      }
    }
  }

  private def reloadQuotes() {
    console.log("Updating Favorite Symbols...")
    /*
    if (mySession.getFavoriteSymbols().nonEmpty) loadQuotes(Favorites, mySession.getFavoriteSymbols())
    if (mySession.getRecentSymbols().nonEmpty) loadQuotes(Recents, mySession.getFavoriteSymbols())
    */
  }

  private def removeFavoriteSymbol(symbol: String) = {
    mySession.userProfile._id foreach { userId =>
      profileService.removeFavoriteSymbol(userId, symbol) onComplete {
        case Success(response) =>
        case Failure(e) =>
          toaster.error("Failed to remove favorite symbol")
          console.error(s"Failed to remove favorite symbol: ${e.getMessage}")
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Event Listeners
  /////////////////////////////////////////////////////////////////////////////

  private val scope = $scope.asInstanceOf[Scope]

  /**
   * Listen for changes to the player's profile
   */
  scope.$on(UserProfileChanged, (profile: UserProfile) => reloadQuotes())

}

/**
 * My Symbols Controller
 * @author lawrence.daniels@gmail.com
 */
object MyQuotesController {
  private val Favorites = "Favorites"
  private val Held = "Held Securities"
  private val Recents = "Recently Viewed"

  private val QuoteLists = js.Array(
    Favorites -> "fa-heart",
    Held -> "fa-star",
    Recents -> "fa-history"
  )

  @ScalaJSDefined
  class Expandable(var icon: String = null,
                   var quotes: js.Array[BasicQuote] = null,
                   var expanded: Boolean = false,
                   var loading: Boolean = false) extends js.Object

}