package com.shocktrade.client.contest

import com.shocktrade.client.GameState._
import com.shocktrade.client.GlobalLoading
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.DashboardController.DashboardRouteParams
import com.shocktrade.client.contest.PositionsController.PositionsControllerScope
import com.shocktrade.client.dialogs.NewOrderDialog
import com.shocktrade.client.dialogs.NewOrderDialogController.{NewOrderDialogResult, NewOrderParams}
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.models.contest.Position
import com.shocktrade.client.users.UserService
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope, Timeout, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Positions Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PositionsController($scope: PositionsControllerScope, $routeParams: DashboardRouteParams,
                          $cookies: Cookies, $timeout: Timeout, toaster: Toaster,
                          @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
                          @injected("PortfolioService") portfolioService: PortfolioService,
                          @injected("UserService") userService: UserService)
  extends Controller with GlobalLoading {

  implicit val cookies: Cookies = $cookies

  $scope.selectedPosition = js.undefined
  $scope.userProfile = js.undefined

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $scope.initPositions = () => for (contestID <- $routeParams.contestID; userID <- $cookies.getGameState.userID) {
    initPositions(contestID, userID)
  }

  $scope.onUserProfileUpdated { (_, _) => $scope.initPositions() }

  private def initPositions(contestID: String, userID: String): Unit = {
    // attempt to load the user profile
    $cookies.getGameState.userID foreach { userID =>
      userService.findUserByID(userID) onComplete {
        case Success(userProfile) => $scope.$apply(() => $scope.userProfile = userProfile.data)
        case Failure(e) => console.error(s"Failed to retrieve user profile: ${e.getMessage}")
      }
    }

    // attempt to load the positions
    portfolioService.findPositions(contestID, userID) onComplete {
      case Success(orders) => $scope.$apply(() => $scope.positions = orders.data)
      case Failure(e) =>
        toaster.error("Failed to retrieve orders")
        console.error(s"Failed to retrieve orders: ${e.displayMessage}")
    }
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
      userID <- $cookies.getGameState.userID
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
  trait PositionsControllerScope extends Scope {
    // functions
    var initPositions: js.Function0[Unit] = js.native
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