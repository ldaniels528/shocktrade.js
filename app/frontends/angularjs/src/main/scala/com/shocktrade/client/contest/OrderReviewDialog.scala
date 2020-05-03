package com.shocktrade.client.contest

import com.shocktrade.client.GameStateService
import com.shocktrade.client.contest.OrderReviewDialog._
import com.shocktrade.client.users.{PersonalSymbolSupport, PersonalSymbolSupportScope}
import com.shocktrade.common.Ok
import com.shocktrade.common.models.contest.Order
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Order Review Dialog
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class OrderReviewDialog($uibModal: Modal) extends Service {

  /**
   * Instantiates the dialog
   * @param orderID the given order ID
   * @return a promise of a [[OrderReviewDialogResult]]
   */
  def popup(orderID: String): js.Promise[OrderReviewDialogResult] = {
    val $uibModalInstance = $uibModal.open[OrderReviewDialogResult](new ModalOptions(
      templateUrl = "order_review_dialog.html",
      controller = classOf[OrderReviewDialogController].getSimpleName,
      resolve = js.Dictionary("orderID" -> (() => orderID))
    ))
    $uibModalInstance.result
  }

}

/**
 * Order Review Dialog Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderReviewDialog {
  type OrderReviewDialogResult = Ok

  /**
   * Order Review Dialog Controller
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  case class OrderReviewDialogController($scope: OrderReviewDialogScope, $uibModalInstance: ModalInstance[OrderReviewDialogResult],
                                         $timeout: Timeout, toaster: Toaster,
                                         @injected("GameStateService") gameStateService: GameStateService,
                                         @injected("PortfolioService") portfolioService: PortfolioService,
                                         @injected("orderID") orderID: () => String)
    extends Controller with PersonalSymbolSupport {

    $scope.errors = emptyArray[String]

    ///////////////////////////////////////////////////////////////////////////
    //          Initialization Functions
    ///////////////////////////////////////////////////////////////////////////

    $scope.initOrderReview = () => {
      console.info(s"Loading order '${orderID()}'...'")
      val outcome = portfolioService.findOrderByID(orderID())
      outcome onComplete {
        case Success(order) => $scope.$apply(() => $scope.order = order.data)
        case Failure(e) =>
          toaster.error("Failed to initialize")
          console.error(s"Failed to initialize order ${orderID()}: ${JSON.stringify(e.getMessage.asInstanceOf[js.Any])}")
      }
      outcome
    }

    ///////////////////////////////////////////////////////////////////////////
    //          Public Functions
    ///////////////////////////////////////////////////////////////////////////

    $scope.cancelOrder = () => {
      $scope.errors.removeAll()
      $scope.isCancelingOrder = true
      val outcome = portfolioService.cancelOrder(orderID())
      outcome onComplete { _ => $timeout(() => $scope.isCancelingOrder = false, 500.millis) }
      outcome onComplete {
        case Success(ok) => $uibModalInstance.close(ok.data)
        case Failure(e) => toaster.info("Failed to cancel order")
      }
      outcome
    }

    $scope.computeOrderCost = (anOrder: js.UndefOr[Order]) => anOrder.flatMap(_.totalCost)

    $scope.dismiss = () => $uibModalInstance.dismiss("dismiss")

  }

  /**
   * Order Review Dialog Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait OrderReviewDialogScope extends Scope with PersonalSymbolSupportScope {
    // functions
    var cancelOrder: js.Function0[js.Promise[HttpResponse[Ok]]] = js.native
    var computeOrderCost: js.Function1[js.UndefOr[Order], js.UndefOr[Double]] = js.native
    var dismiss: js.Function0[Unit] = js.native
    var initOrderReview: js.Function0[js.Promise[HttpResponse[Order]]] = js.native

    // variables
    var errors: js.Array[String] = js.native
    var isCancelingOrder: js.UndefOr[Boolean] = js.native
    var order: js.UndefOr[Order] = js.native

  }

  /**
   * Order Review Dialog Popup Support
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  trait OrderReviewDialogPopupSupport {
    ref: Controller =>

    def $scope: OrderReviewDialogPopupSupportScope

    def orderReviewDialog: OrderReviewDialog

    def toaster: Toaster

    $scope.popupOrderReviewDialog = (anOrderID: js.UndefOr[String]) => anOrderID map { orderID =>
      val outcome = orderReviewDialog.popup(orderID)
      outcome.toFuture onComplete {
        case Success(response) =>
          console.info(s"response = ${JSON.stringify(response)}")
        case Failure(e) =>
          if(!e.getMessage.contains("dismiss")) {
            toaster.error("Failed to retrieve order")
            console.error(s"Failed to retrieve order $orderID: ${e.displayMessage}")
          }
      }
      outcome
    }

  }

  /**
   * Order Review Dialog Popup Support Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait OrderReviewDialogPopupSupportScope extends Scope {
    var popupOrderReviewDialog: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[OrderReviewDialogResult]]] = js.native
  }

}