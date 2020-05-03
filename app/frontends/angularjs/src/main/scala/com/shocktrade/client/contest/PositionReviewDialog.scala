package com.shocktrade.client.contest

import com.shocktrade.client.GameStateService
import com.shocktrade.client.contest.PositionReviewDialog._
import com.shocktrade.client.dialogs.NewOrderDialog
import com.shocktrade.client.dialogs.NewOrderDialog.NewOrderDialogResult
import com.shocktrade.client.users.{PersonalSymbolSupport, PersonalSymbolSupportScope}
import com.shocktrade.common.OrderConstants._
import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.common.models.contest.Position
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper.{emptyArray, _}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Position Review Dialog
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PositionReviewDialog($uibModal: Modal) extends Service {

  /**
   * Instantiates the dialog
   * @param contestID  the given contest ID
   * @param positionID the given position ID
   * @return a promise of a [[PositionReviewDialogResult]]
   */
  def popup(contestID: String, positionID: String): js.Promise[PositionReviewDialogResult] = {
    val $uibModalInstance = $uibModal.open[PositionReviewDialogResult](new ModalOptions(
      templateUrl = "position_review_dialog.html",
      controller = classOf[PositionReviewDialogController].getSimpleName,
      resolve = js.Dictionary("contestID" -> (() => contestID), "positionID" -> (() => positionID))
    ))
    $uibModalInstance.result
  }

}

/**
 * Position Review Dialog Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PositionReviewDialog {
  type PositionReviewDialogResult = NewOrderForm

  /**
   * Position Review Dialog Controller
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  case class PositionReviewDialogController($scope: PositionReviewDialogScope, $uibModalInstance: ModalInstance[PositionReviewDialogResult],
                                            $timeout: Timeout, toaster: Toaster,
                                            @injected("GameStateService") gameStateService: GameStateService,
                                            @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
                                            @injected("PortfolioService") portfolioService: PortfolioService,
                                            @injected("contestID") contestID: () => String,
                                            @injected("positionID") positionID: () => String)
    extends Controller with PersonalSymbolSupport {

    $scope.errors = emptyArray[String]

    ///////////////////////////////////////////////////////////////////////////
    //          Initialization Functions
    ///////////////////////////////////////////////////////////////////////////

    $scope.initPositionReview = () => {
      console.info(s"Loading position '${positionID()}'...'")
      val outcome = portfolioService.findPositionByID(positionID())
      outcome onComplete {
        case Success(position) => $scope.$apply(() => $scope.position = position.data)
        case Failure(e) =>
          toaster.error("Failed to initialize")
          console.error(s"Failed to initialize position ${positionID()}: ${JSON.stringify(e.getMessage.asInstanceOf[js.Any])}")
      }
      outcome
    }

    ///////////////////////////////////////////////////////////////////////////
    //          Public Functions
    ///////////////////////////////////////////////////////////////////////////

    $scope.sellPosition = () => {
      $scope.errors.removeAll()
      for {
        userID <- gameStateService.getUserID
        symbol <- $scope.position.flatMap(_.symbol)
        quantity <- $scope.position.flatMap(_.quantity)
      } yield {
        $uibModalInstance.dismiss("dismiss")
        newOrderDialog.popup(contestID(), userID, NewOrderForm(
          symbol = symbol, quantity = quantity, priceType = Limit, orderType = SELL, orderTerm = "3"
        ))
      }
    }

    $scope.dismiss = () => $uibModalInstance.dismiss("dismiss")

  }

  /**
   * Position Review Dialog Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait PositionReviewDialogScope extends Scope with PersonalSymbolSupportScope {
    // functions
    var sellPosition: js.Function0[js.UndefOr[js.Promise[NewOrderDialogResult]]] = js.native
    var dismiss: js.Function0[Unit] = js.native
    var initPositionReview: js.Function0[js.Promise[HttpResponse[Position]]] = js.native

    // variables
    var errors: js.Array[String] = js.native
    var position: js.UndefOr[Position] = js.native

  }

  /**
   * Position Review Dialog Popup Support
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  trait PositionReviewDialogPopupSupport {
    ref: Controller =>

    def $scope: PositionReviewDialogPopupSupportScope

    def positionReviewDialog: PositionReviewDialog

    def toaster: Toaster

    $scope.popupPositionReviewDialog = (aContestID: js.UndefOr[String], anPositionID: js.UndefOr[String]) => {
      for {
        contestID <- aContestID
        positionID <- anPositionID
      } yield {
        val outcome = positionReviewDialog.popup(contestID, positionID)
        outcome.toFuture onComplete {
          case Success(response) =>
            console.info(s"response = ${JSON.stringify(response)}")
          case Failure(e) =>
            if (!e.getMessage.contains("dismiss")) {
              toaster.error("Failed to retrieve position")
              console.error(s"Failed to retrieve position $positionID: ${e.displayMessage}")
            }
        }
        outcome
      }
    }

  }

  /**
   * Position Review Dialog Popup Support Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait PositionReviewDialogPopupSupportScope extends Scope {
    var popupPositionReviewDialog: js.Function2[js.UndefOr[String], js.UndefOr[String], js.UndefOr[js.Promise[PositionReviewDialogResult]]] = js.native
  }

}