package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.DashboardController.DashboardRouteParams
import com.shocktrade.client.contest.PositionsController.PositionsControllerScope
import com.shocktrade.client.dialogs.NewOrderDialog
import com.shocktrade.client.dialogs.NewOrderDialogController.{NewOrderDialogResult, NewOrderParams}
import com.shocktrade.client.users.{PersonalSymbolSupport, PersonalSymbolSupportScope, UserService}
import com.shocktrade.client.{GameStateService, GlobalLoading}
import com.shocktrade.common.models.contest.Position
import com.shocktrade.common.models.user.UserProfile
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Positions Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class PositionsController($scope: PositionsControllerScope, $routeParams: DashboardRouteParams, toaster: Toaster,
                               @injected("GameStateService") gameStateService: GameStateService,
                               @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
                               @injected("PortfolioService") portfolioService: PortfolioService,
                               @injected("UserService") userService: UserService)
  extends Controller with GlobalLoading with PersonalSymbolSupport {

  $scope.selectedPosition = js.undefined
  $scope.userProfile = js.undefined

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $scope.initPositions = () => for (contestID <- $routeParams.contestID; userID <- gameStateService.getUserID) yield {
    initPositions(contestID, userID)
  }

  $scope.onUserProfileUpdated { (_, _) => $scope.initPositions() }

  private def initPositions(contestID: String, userID: String): js.Promise[(HttpResponse[UserProfile], HttpResponse[js.Array[Position]])] = {
    val outcome = for {
      userProfile <- userService.findUserByID(userID)
      positions <- portfolioService.findPositions(contestID, userID)
    } yield (userProfile, positions)

    outcome onComplete {
      case Success((userProfile, positions)) =>
        $scope.$apply { () =>
          $scope.positions = positions.data
          $scope.userProfile = userProfile.data
        }
      case Failure(e) =>
        toaster.error("Failed to retrieve positions")
        console.error(s"Failed to retrieve positions: ${e.displayMessage}")
    }

    outcome.toJSPromise
  }

  /////////////////////////////////////////////////////////////////////
  //          Position Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getPositions = () => $scope.positions

  $scope.isPositionSelected = () => $scope.getPositions().nonEmpty && $scope.selectedPosition.nonEmpty

  $scope.selectPosition = (position: js.UndefOr[Position]) => $scope.selectedPosition = position

  $scope.sellPosition = (aSymbol: js.UndefOr[String], aQuantity: js.UndefOr[Double]) => {
    for {
      contestID <- $routeParams.contestID
      userID <- gameStateService.getUserID
      symbol <- aSymbol
      quantity <- aQuantity
    } yield newOrderDialog.popup(new NewOrderParams(contestID, userID, symbol = symbol, quantity = quantity))
  }

  $scope.toggleSelectedPosition = () => $scope.selectedPosition = js.undefined

}

/**
 * Positions Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PositionsController {

  /**
   * Positions Controller Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait PositionsControllerScope extends Scope with PersonalSymbolSupportScope {
    // functions
    var initPositions: js.Function0[js.UndefOr[js.Promise[(HttpResponse[UserProfile], HttpResponse[js.Array[Position]])]]] = js.native
    var getPositions: js.Function0[js.UndefOr[js.Array[Position]]] = js.native
    var isPositionSelected: js.Function0[Boolean] = js.native
    var selectPosition: js.Function1[js.UndefOr[Position], Unit] = js.native
    var sellPosition: js.Function2[js.UndefOr[String], js.UndefOr[Double], js.UndefOr[js.Promise[NewOrderDialogResult]]] = js.native
    var toggleSelectedPosition: js.Function0[Unit] = js.native

    // variables
    var positions: js.UndefOr[js.Array[Position]] = js.native
    var selectedPosition: js.UndefOr[Position] = js.native
    var userProfile: js.UndefOr[UserProfile] = js.native
  }

}