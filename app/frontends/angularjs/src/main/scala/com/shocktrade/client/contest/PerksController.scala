package com.shocktrade.client.contest

import com.shocktrade.client.GameState._
import com.shocktrade.client.RootScope
import com.shocktrade.client.contest.DashboardController.DashboardRouteParams
import com.shocktrade.client.contest.PerksController.PerksControllerScope
import com.shocktrade.client.users.UserService
import com.shocktrade.common.forms.PerksResponse
import com.shocktrade.common.models.contest.{Perk, Portfolio}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Timeout, injected}
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Perks Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class PerksController($scope: PerksControllerScope, $routeParams: DashboardRouteParams,
                           $cookies: Cookies, $timeout: Timeout, toaster: Toaster,
                           @injected("PortfolioService") portfolioService: PortfolioService,
                           @injected("UserService") userService: UserService) extends Controller {

  $scope.availablePerks = Perk.availablePerks
  $scope.myPerkCodes = emptyArray[String]

  private var perkMapping = js.Dictionary[Perk]()

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initPerks = () => {
    console.info(s"Initializing ${getClass.getSimpleName}...")
    for {
      contestID <- $routeParams.contestID
      userID <- $cookies.getGameState.userID
    } yield initPerks(contestID, userID)
  }

  private def initPerks(contestID: String, userID: String): js.Promise[HttpResponse[Portfolio]] = {
    console.info(s"Loading portfolio for contest $contestID user $userID...")
    val outcome = portfolioService.findPortfolioByUser(contestID, userID)
    outcome onComplete {
      case Success(portfolio) =>
        $scope.$apply { () => $scope.portfolio = portfolio.data }
      case Failure(e) =>
        toaster.error(e.displayMessage)
    }
    outcome
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.countOwnedPerks = () => $scope.availablePerks.count(_.owned.isTrue)

  $scope.isPerksSelected = () => $scope.availablePerks.exists(p => p.selected.isTrue && !p.owned.isTrue)

  $scope.getPerkNameClass = (aPerk: js.UndefOr[Perk]) => "st_bkg_color"

  $scope.getPerkDescClass = (aPerk: js.UndefOr[Perk]) => ""

  $scope.loadPerks = () => $scope.portfolio.flatMap(_.portfolioID) map loadPerks

  private def loadPerks(portfolioID: String): js.Promise[HttpResponse[PerksResponse]] = {
    // load the player's perks
    val outcome = portfolioService.findPurchasedPerks(portfolioID)
    outcome onComplete {
      case Success(perksResponse) =>
        // create a mapping from the available perks
        this.perkMapping = js.Dictionary(Perk.availablePerks.map(p => p.code -> p): _*)

        // capture the owned perk codes
        //$scope.fundsAvailable = perksResponse.fundsAvailable
        $scope.myPerkCodes = perksResponse.data.perkCodes

        $scope.$apply(() => setupPerks())
      case Failure(e) =>
        toaster.error("Error loading perks from the portfolio")
        console.error(s"Error loading player perks: ${e.displayMessage}")
    }
    outcome
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Setup the perks state; indicating which perks are owned
   */
  private def setupPerks() {
    $scope.availablePerks.foreach { perk =>
      perk.owned = $scope.myPerkCodes.exists(_.contains(perk.code))
      perk.selected = perk.owned
    }
  }

}

/**
 * Perks Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PerksController {

  @js.native
  trait PerksControllerScope extends RootScope {
    // variables
    var availablePerks: js.Array[Perk] = js.native
    var myPerkCodes: js.UndefOr[js.Array[String]] = js.native
    var portfolio: js.UndefOr[Portfolio] = js.native

    // functions
    var initPerks: js.Function0[js.UndefOr[js.Promise[HttpResponse[Portfolio]]]] = js.native
    var countOwnedPerks: js.Function0[Int] = js.native
    var isPerksSelected: js.Function0[Boolean] = js.native
    var getPerkNameClass: js.Function1[js.UndefOr[Perk], js.UndefOr[String]] = js.native
    var getPerkDescClass: js.Function1[js.UndefOr[Perk], js.UndefOr[String]] = js.native
    var loadPerks: js.Function0[js.UndefOr[js.Promise[HttpResponse[PerksResponse]]]] = js.native
  }

}