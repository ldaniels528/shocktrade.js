package com.shocktrade.javascript.dialogs

import biz.enef.angulate.core.{HttpPromise, HttpService}
import biz.enef.angulate.{ScopeController, Service, named}
import com.greencatsoft.angularjs.core.Promise
import com.greencatsoft.angularjs.extensions.{ModalInstance, ModalOptions, ModalService}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.TransferFundsDialog.TransferFundsDialogController
import prickle.Unpickle

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
 * Transfer Funds Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class TransferFundsDialog($http: HttpService, $modal: ModalService) extends Service {

  /**
   * Transfer Funds pop-up dialog
   */
  @JSExport
  def popup: js.Function0[Promise] = () => {
    val options = ModalOptions()
    options.templateUrl = "transfer_funds_dialog.htm"
    options.controller = classOf[TransferFundsDialogController].getSimpleName

    val modalInstance = $modal.open(options)
    modalInstance.result
  }

  def transferFunds: js.Function3[String, String, js.Dynamic, HttpPromise[js.Dynamic]] = (contestId: String, playerId: String, form: js.Dynamic) => {
    $http.post[js.Dynamic](s"/api/contest/$contestId/margin/$playerId", form)
  }

}

/**
 * Transfer Funds Dialog Singleton
 * @author lawrence.daniels@gmail.com
 */
object TransferFundsDialog {
  private val CASH = "CASH"
  private val MARGIN = "MARGIN"

  private val transferActions = js.Array(
    JS(label = "Cash to Margin Account", source = CASH),
    JS(label = "Margin Account to Cash", source = MARGIN))

  /**
   * Transfer Funds Dialog Controller
   * @author lawrence.daniels@gmail.com
   */
  class TransferFundsDialogController($scope: js.Dynamic, $modalInstance: ModalInstance, toaster: Toaster,
                                      @named("MySession") mySession: MySession,
                                      @named("TransferFundsDialog") dialog: TransferFundsDialog)
    extends ScopeController {

    private val messages = emptyArray[String]

    $scope.actions = transferActions

    $scope.form = JS(
      cashFunds = mySession.getCashAccount().cashFunds,
      marginFunds = mySession.getMarginAccount().cashFunds,
      initialMargin = mySession.getMarginAccount().initialMargin,
      action = null,
      amount = null
    )

    $scope.init = () => {
      // TODO compute the net value of the stock in the margin account
    }

    $scope.getMessages = () => messages

    $scope.hasMessages = () => messages.nonEmpty

    $scope.accept = (form: js.Dynamic) => accept(form)

    $scope.cancel = () => $modalInstance.dismiss("cancel")

    private def accept(form: js.Dynamic) {
      if (isValidated(form)) {
        dialog.transferFunds(mySession.getContestID(), mySession.getUserID(), form) onComplete {
          case Success(response) => $modalInstance.close(response)
          case Failure(e) => messages.push("Failed to deposit funds")
        }
      }
    }

    private def convertForm(formJs: js.Dynamic) = Unpickle[TransferFundsForm].fromString(JSON.stringify(formJs))

    /**
     * Validates the given transfer funds form
     * @param formJs the given [[TransferFundsForm transfer funds form]]
     * @return true, if the form does not contain errors
     */
    private def isValidated(formJs: js.Dynamic) = {
      // clear the messages
      messages.remove(0, messages.length)

      // first, perform coarse validation
      if (!isDefined(formJs.action)) messages.push("Please select an Action")
      else if (!isDefined(formJs.amount)) messages.push("Please enter the desired amount")
      else {
        // next, perform fine-grained validation
        convertForm(formJs) match {
          case Success(form) =>
            if (form.amount <= 0) messages.push("Please enter an amount greater than zero")
            if (isInsufficientCashFunds(form)) messages.push("Insufficient funds in your cash account to complete the request")
            if (isInsufficientMarginFunds(form)) messages.push("Insufficient funds in your margin account to complete the request")
          case Failure(e) =>
            messages.push("An Internal Error occurred. Try again later.")
            g.console.log(s"formJs => ${JSON.stringify(formJs)}")
            g.console.error(s"Internal Error: ${e.getMessage}")
        }
      }

      messages.isEmpty
    }

    private def isInsufficientCashFunds(form: TransferFundsForm) = {
      form.action.source == CASH && form.amount > form.cashFunds
    }

    private def isInsufficientMarginFunds(form: TransferFundsForm) = {
      form.action.source == MARGIN && form.amount > form.marginFunds
    }

  }

  case class TransferFundsForm(action: TransferFundsAction, amount: Double, cashFunds: Double, marginFunds: Double)

  case class TransferFundsAction(label: String, source: String)

}