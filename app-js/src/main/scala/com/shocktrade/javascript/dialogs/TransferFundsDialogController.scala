package com.shocktrade.javascript.dialogs

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.core.{Controller, ModalInstance}
import com.ldaniels528.javascript.angularjs.extensions.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.TransferFundsDialogController._
import prickle.Unpickle

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Transfer Funds Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class TransferFundsDialogController($scope: js.Dynamic, $modalInstance: ModalInstance[js.Dynamic], toaster: Toaster,
                                    @named("MySession") mySession: MySession,
                                    @named("TransferFundsDialog") dialog: TransferFundsDialogService)
  extends Controller {

  private val messages = emptyArray[String]

  $scope.actions = transferActions

  $scope.form = JS(
    cashFunds = mySession.getCashAccount().cashFunds,
    marginFunds = mySession.getMarginAccount().cashFunds,
    initialMargin = mySession.getMarginAccount().initialMargin,
    action = null,
    amount = null
  )

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    // TODO compute the net value of the stock in the margin account
  }

  $scope.getMessages = () => messages

  $scope.hasMessages = () => messages.nonEmpty

  $scope.accept = (form: js.Dynamic) => accept(form)

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

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

/**
 * Transfer Funds Dialog Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object TransferFundsDialogController {
  private val CASH = "CASH"
  private val MARGIN = "MARGIN"

  private val transferActions = js.Array(
    JS(label = "Cash to Margin Account", source = CASH),
    JS(label = "Margin Account to Cash", source = MARGIN))

  case class TransferFundsForm(action: TransferFundsAction, amount: Double, cashFunds: Double, marginFunds: Double)

  case class TransferFundsAction(label: String, source: String)

}