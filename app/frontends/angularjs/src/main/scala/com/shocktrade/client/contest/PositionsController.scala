package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.DashboardController.DashboardRouteParams
import com.shocktrade.client.contest.PositionReviewDialog.{PositionReviewDialogPopupSupport, PositionReviewDialogPopupSupportScope}
import com.shocktrade.client.contest.PositionsController.PositionsControllerScope
import com.shocktrade.client.dialogs.NewOrderDialog
import com.shocktrade.client.dialogs.NewOrderDialog.NewOrderDialogResult
import com.shocktrade.client.users.{PersonalSymbolSupport, PersonalSymbolSupportScope, UserService}
import com.shocktrade.client.{GameStateService, GlobalLoading}
import com.shocktrade.common.forms.NewOrderForm
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
                               @injected("PositionReviewDialog") positionReviewDialog: PositionReviewDialog,
                               @injected("UserService") userService: UserService)
  extends Controller with GlobalLoading with PersonalSymbolSupport with PositionReviewDialogPopupSupport {

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

  $scope.sellPosition = (aSymbol: js.UndefOr[String], aQuantity: js.UndefOr[Double]) => {
    for {
      contestID <- $routeParams.contestID
      userID <- gameStateService.getUserID
      symbol <- aSymbol
      quantity <- aQuantity
    } yield newOrderDialog.popup(contestID, userID, NewOrderForm(symbol = symbol, quantity = quantity))
  }

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
  trait PositionsControllerScope extends Scope with PersonalSymbolSupportScope with PositionReviewDialogPopupSupportScope {
    // functions
    var initPositions: js.Function0[js.UndefOr[js.Promise[(HttpResponse[UserProfile], HttpResponse[js.Array[Position]])]]] = js.native
    var getPositions: js.Function0[js.UndefOr[js.Array[Position]]] = js.native
    var sellPosition: js.Function2[js.UndefOr[String], js.UndefOr[Double], js.UndefOr[js.Promise[NewOrderDialogResult]]] = js.native

    // variables
    var positions: js.UndefOr[js.Array[Position]] = js.native
    var userProfile: js.UndefOr[UserProfile] = js.native
  }

}