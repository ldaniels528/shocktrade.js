package com.shocktrade.client.users

import com.shocktrade.client.RootScope
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.{ContestService, PortfolioService}
import com.shocktrade.client.discover.QuoteService
import com.shocktrade.client.users.MyQuotesController._
import com.shocktrade.common.models.quote.{OrderQuote, ResearchQuote}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Location, _}
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * My Quotes Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class MyQuotesController($scope: MyQuotesControllerScope, $location: Location, $q: Q, toaster: Toaster,
                              @injected("ContestService") contestService: ContestService,
                              @injected("PortfolioService") portfolioService: PortfolioService,
                              @injected("UserService") userService: UserService,
                              @injected("QuoteService") quoteService: QuoteService)
  extends Controller with PersonalSymbolSupport {

  private val quoteSets = js.Dictionary(quoteLists map { case (name, icon) =>
    name -> new Expandable(icon = icon, quotes = null, expanded = false, loading = false)
  }: _*)

  $scope.selectedQuote = js.undefined

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.isSelected = (aQuote: js.UndefOr[ResearchQuote]) => aQuote.exists($scope.selectedQuote.contains)

  $scope.selectQuote = (aQuote: js.UndefOr[ResearchQuote]) => $scope.selectedQuote = aQuote

  $scope.expandList = (aName: js.UndefOr[String]) => aName foreach expandList

  $scope.getQuoteSets = () => quoteSets

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def expandList(name: String) {
    quoteSets.get(name) foreach { obj =>
      obj.expanded = !obj.expanded
      if (obj.expanded && !isDefined(obj.quotes)) {
        obj.quotes = emptyArray[OrderQuote]
        name match {
          case Favorites => loadQuotes(name, $scope.favoriteSymbols, obj)
          case Held => loadHeldSecurities(obj)
          case Recents => loadQuotes(name, $scope.recentSymbols, obj)
          case _ =>
            console.error(s"$name is not a recognized list")
        }
      }
    }
  }

  private def loadQuotes(name: String, symbols: js.Array[String], obj: Expandable) {
    if (symbols.nonEmpty) {
      quoteService.getBasicQuotes(symbols) onComplete {
        case Success(updatedQuotes) => $scope.$apply(() => obj.quotes = updatedQuotes.data)
        case Failure(e) =>
          toaster.error(s"Failed to load $name")
          console.error(s"Failed to load $name: ${e.getMessage}")
      }
    }
  }

  private def loadHeldSecurities(obj: Expandable): Unit = {
    for {
      portfolioID <- $scope.userProfile.flatMap(_.userID)
    } {
      val outcome = for {
        symbols <- portfolioService.findHeldSecurities(portfolioID).map(_.data)
        quotes <- quoteService.getBasicQuotes(symbols).map(_.data)
      } yield quotes

      outcome onComplete {
        case Success(updatedQuotes) => $scope.$apply(() => obj.quotes = updatedQuotes)
        case Failure(e) =>
          toaster.error("Failed to load Held Securities")
          console.error(s"Failed to load Held Securities: ${e.getMessage}")
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Event Listeners
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Listen for changes to the player's profile
   */
  $scope.onUserProfileUpdated((_, profile) => profile.userID.foreach(refreshMySymbols))

}

/**
 * My Quotes Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object MyQuotesController {
  private val Favorites = "Favorites"
  private val Held = "Held Securities"
  private val Recents = "Recently Viewed"

  private val quoteLists = js.Array(
    Favorites -> "fa-heart",
    Held -> "fa-star",
    Recents -> "fa-history"
  )

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
trait MyQuotesControllerScope extends RootScope with PersonalSymbolSupportScope {
  // variables
  var selectedQuote: js.UndefOr[ResearchQuote] = js.native
  //var userProfile: js.UndefOr[UserProfile] = js.native

  // quote-related functions
  var expandList: js.Function1[js.UndefOr[String], Unit] = js.native
  var getQuoteSets: js.Function0[js.Dictionary[Expandable]] = js.native
  var isSelected: js.Function1[js.UndefOr[ResearchQuote], Boolean] = js.native
  var selectQuote: js.Function1[js.UndefOr[ResearchQuote], Unit] = js.native

}