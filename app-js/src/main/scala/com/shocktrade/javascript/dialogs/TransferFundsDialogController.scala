package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.extensions.{ModalInstance, Toaster}
import com.github.ldaniels528.scalascript.{Controller, Scope, injected, scoped}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.TransferFundsDialogController._
import com.shocktrade.javascript.models.Contest

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Transfer Funds Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class TransferFundsDialogController($scope: TransferFundsScope, $modalInstance: ModalInstance[Contest], toaster: Toaster,
                                    @injected("MySession") mySession: MySession,
                                    @injected("TransferFundsDialog") dialog: TransferFundsDialogService)
  extends Controller {

  private val messages = emptyArray[String]

  $scope.actions = transferActions

  $scope.form = {
    val form = TransferFundsForm()
    form.cashFunds = mySession.getCashAccount.cashFunds
    form.marginFunds = mySession.getMarginAccount.cashFunds
    form.initialMargin = mySession.getMarginAccount.initialMargin
    form
  }

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  @scoped def init() = {
    // TODO compute the net value of the stock in the margin account
  }

  @scoped def getMessages = messages

  @scoped def hasMessages = messages.nonEmpty

  @scoped def cancel() = $modalInstance.dismiss("cancel")

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  @scoped
  def accept(form: TransferFundsForm) {
    if (isValidated(form)) {
      (for {
        contestId <- mySession.contest.flatMap(_.OID_?)
        userId <- mySession.userProfile.OID_?
      } yield {
          dialog.transferFunds(contestId, userId, form) onComplete {
            case Success(response) => $modalInstance.close(response)
            case Failure(e) => messages.push("Failed to deposit funds")
          }
        }) getOrElse toaster.error("No game selected")
    }
  }

  /**
   * Validates the given transfer funds form
   * @param form the given [[TransferFundsForm transfer funds form]]
   * @return true, if the form does not contain errors
   */
  private def isValidated(form: TransferFundsForm) = {
    // clear the messages
    messages.remove(0, messages.length)

    // first, perform coarse validation
    if (form.action == null) messages.push("Please select an Action")
    else if (form.amount.isEmpty) messages.push("Please enter the desired amount")
    else {
      // next, perform fine-grained validation
      if (form.amount.exists(_ <= 0)) messages.push("Please enter an amount greater than zero")
      if (isInsufficientCashFunds(form)) messages.push("Insufficient funds in your cash account to complete the request")
      if (isInsufficientMarginFunds(form)) messages.push("Insufficient funds in your margin account to complete the request")
    }

    messages.isEmpty
  }

  private def isInsufficientCashFunds(form: TransferFundsForm): Boolean = {
    (for {
      action <- Option(form.action) if action.source == CASH
      amount <- form.amount.toOption
      cashFunds <- form.cashFunds.toOption
    } yield amount > cashFunds).contains(true)
  }

  private def isInsufficientMarginFunds(form: TransferFundsForm) = {
    (for {
      action <- Option(form.action) if action.source == MARGIN
      amount <- form.amount.toOption
      marginFunds <- form.marginFunds.toOption
    } yield amount > marginFunds).contains(true)
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
    TransferFundsAction(label = "Cash to Margin Account", source = CASH),
    TransferFundsAction(label = "Margin Account to Cash", source = MARGIN))

}

/**
 * Transfer Funds Scope
 */
trait TransferFundsScope extends Scope {
  var actions: js.Array[TransferFundsAction] = js.native
  var form: TransferFundsForm = js.native
}

/**
 * Transfer Funds Form
 */
trait TransferFundsForm extends js.Object {
  var action: TransferFundsAction = js.native
  var initialMargin: Double = js.native
  var amount: js.UndefOr[Double] = js.native
  var cashFunds: js.UndefOr[Double] = js.native
  var marginFunds: js.UndefOr[Double] = js.native
}

/**
 * Transfer Funds Form Singleton
 */
object TransferFundsForm {

  def apply() = makeNew[TransferFundsForm]
}

/**
 * Transfer Funds Action
 */
trait TransferFundsAction extends js.Object {
  var label: String = js.native
  var source: String = js.native
}

/**
 * Transfer Funds Action Singleton
 */
object TransferFundsAction {

  def apply(label: String, source: String) = {
    val action = makeNew[TransferFundsAction]
    action.label = label
    action.source = source
    action
  }
}