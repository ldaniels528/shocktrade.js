package com.shocktrade.client.profile

import com.shocktrade.client.MySessionService
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.{ContestService, PortfolioService}
import com.shocktrade.client.discover.QuoteService
import com.shocktrade.client.profile.MyQuotesController._
import com.shocktrade.common.models.quote.{OrderQuote, ResearchQuote}
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Location, _}
import org.scalajs.dom.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * My Quotes Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class MyQuotesController($scope: MyQuotesControllerScope, $location: Location, toaster: Toaster,
                         @injected("ContestService") contestService: ContestService,
                         @injected("MySessionService") mySession: MySessionService,
                         @injected("PortfolioService") portfolioService: PortfolioService,
                         @injected("ProfileService") profileService: ProfileService,
                         @injected("QuoteService") quoteService: QuoteService)
  extends Controller {

  private val quoteSets = js.Dictionary(QuoteLists map { case (name, icon) =>
    name -> new Expandable(icon = icon, quotes = null, expanded = false, loading = false)
  }: _*)

  $scope.selectedQuote = js.undefined

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.isSelected = (aQuote: js.UndefOr[ResearchQuote]) => aQuote.exists($scope.selectedQuote == _)

  $scope.selectQuote = (aQuote: js.UndefOr[ResearchQuote]) => $scope.selectedQuote = aQuote

  $scope.expandList = (aName: js.UndefOr[String]) => aName foreach expandList

  $scope.getQuoteSets = () => quoteSets

  $scope.addFavoriteSymbol = (aSymbol: js.UndefOr[String]) => aSymbol foreach addFavoriteSymbol

  $scope.isFavorite = (aSymbol: js.UndefOr[String]) => aSymbol exists mySession.isFavoriteSymbol

  $scope.removeFavoriteSymbol = (aSymbol: js.UndefOr[String]) => aSymbol foreach removeFavoriteSymbol

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
        obj.quotes = emptyArray[OrderQuote]
        name match {
          case Favorites => loadQuotes(name, mySession.getFavoriteSymbols getOrElse emptyArray, obj)
          case Held => loadHeldSecurities(obj)
          case Recents => loadQuotes(name, mySession.getRecentSymbols getOrElse emptyArray, obj)
          case _ =>
            console.error(s"$name is not a recognized list")
        }
      }
    }
  }

  private def loadQuotes(name: String, symbols: js.Array[String], obj: Expandable) {
    if (symbols.nonEmpty) {
      quoteService.getBasicQuotes(symbols) onComplete {
        case Success(updatedQuotes) => $scope.$apply(() => obj.quotes = updatedQuotes)
        case Failure(e) =>
          toaster.error(s"Failed to load $name")
          console.error(s"Failed to load $name: ${e.getMessage}")
      }
    }
  }

  private def loadHeldSecurities(obj: Expandable): Unit = {
    for {
      playerId <- mySession.userProfile._id
    } {
      val outcome = for {
        symbols <- portfolioService.getHeldSecurities(playerId)
        quotes <- quoteService.getBasicQuotes(symbols)
      } yield quotes

      outcome onComplete {
        case Success(updatedQuotes) => $scope.$apply(() => obj.quotes = updatedQuotes)
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
        case Success(response) => $scope.$apply(() => {})
        case Failure(e) =>
          toaster.error("Failed to remove favorite symbol")
          console.error(s"Failed to remove favorite symbol: ${e.getMessage}")
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Event Listeners
  /////////////////////////////////////////////////////////////////////////////

  /**
    * Listen for changes to the player's profile
    */
  $scope.onUserProfileChanged((_, profile) => reloadQuotes())

}

/**
  * My Quotes Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
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
  class Expandable(val icon: String = null,
                   var quotes: js.Array[OrderQuote] = null,
                   var expanded: Boolean = false,
                   var loading: Boolean = false) extends js.Object

}

/**
  * My Quotes Controller Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait MyQuotesControllerScope extends Scope {
  // variables
  var selectedQuote: js.UndefOr[ResearchQuote] = js.native

  // symbol-related functions
  var addFavoriteSymbol: js.Function1[js.UndefOr[String], Unit] = js.native
  var isFavorite: js.Function1[js.UndefOr[String], Boolean] = js.native
  var removeFavoriteSymbol: js.Function1[js.UndefOr[String], Unit] = js.native

  // quote-related functions
  var expandList: js.Function1[js.UndefOr[String], Unit] = js.native
  var getQuoteSets: js.Function0[js.Dictionary[Expandable]] = js.native
  var isSelected: js.Function1[js.UndefOr[ResearchQuote], Boolean] = js.native
  var selectQuote: js.Function1[js.UndefOr[ResearchQuote], Unit] = js.native

}