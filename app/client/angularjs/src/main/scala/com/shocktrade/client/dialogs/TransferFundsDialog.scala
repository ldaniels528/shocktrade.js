package com.shocktrade.client.dialogs

import com.shocktrade.client.MySessionService
import com.shocktrade.client.contest.PortfolioService
import com.shocktrade.client.dialogs.TransferFundsDialogController._
import com.shocktrade.client.models.contest.Portfolio
import com.shocktrade.common.forms.FundsTransferRequest
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.npm.angularjs.{Controller, Scope, Service, injected}
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Transfer Funds Dialog Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class TransferFundsDialog($modal: Modal) extends Service {

  /**
    * Transfer Funds pop-up dialog
    */
  def popup(): Future[TransferFundsResult] = {
    val modalInstance = $modal.open[TransferFundsResult](new ModalOptions(
      templateUrl = "transfer_funds_dialog.html",
      controller = classOf[TransferFundsDialogController].getSimpleName
    ))
    modalInstance.result
  }

}

/**
  * Transfer Funds Dialog Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class TransferFundsDialogController($scope: TransferFundsScope, $modalInstance: ModalInstance[TransferFundsResult], toaster: Toaster,
                                    @injected("PortfolioService") portfolioService: PortfolioService,
                                    @injected("MySessionService") mySession: MySessionService,
                                    @injected("TransferFundsDialog") dialog: TransferFundsDialog)
  extends Controller {

  private val messages = emptyArray[String]

  $scope.actions = TransferActions

  $scope.form = new TransferFundsForm(
    funds = mySession.cashAccount_?.orUndefined.flatMap(_.funds),
    marginFunds = mySession.marginAccount_?.orUndefined.flatMap(_.funds)
  )

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    // TODO compute the net value of the stock in the margin account
  }

  $scope.getMessages = () => messages

  $scope.hasMessages = () => messages.nonEmpty

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  $scope.accept = (form: TransferFundsForm) => {
    if (isValidated(form)) {
      (for {
        portfolioId <- mySession.portfolio_?.flatMap(_._id.toOption)
      } yield {
        portfolioService.transferFunds(portfolioId, form) onComplete {
          case Success(response) => $modalInstance.close(response.data)
          case Failure(e) => messages.push("Failed to deposit funds")
        }
      }) getOrElse toaster.error("No game selected")
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  /**
    * Validates the given transfer funds form
    * @param form the given [[TransferFundsForm transfer funds form]]
    * @return true, if the form does not contain errors
    */
  private def isValidated(form: TransferFundsForm) = {
    // clear the messages
    messages.removeAll()

    // first, perform coarse validation
    if (form.action.isEmpty) messages.push("Please select an Action")
    else if (form.amount.isEmpty) messages.push("Please enter the desired amount")
    else {
      // next, perform fine-grained validation
      if (form.amount.exists(_ <= 0)) messages.push("Please enter an amount greater than zero")
      if (isInsufficientCashFunds(form)) messages.push("Insufficient funds in your cash account to complete the request")
      if (isInsufficientMarginFunds(form)) messages.push("Insufficient funds in your margin account to complete the request")
    }

    messages.isEmpty
  }

  private def isInsufficientCashFunds(form: TransferFundsForm) = {
    (for {
      action <- form.action.toOption if action.source == CASH
      amount <- form.amount.toOption
      funds <- form.funds.toOption
    } yield amount > funds).contains(true)
  }

  private def isInsufficientMarginFunds(form: TransferFundsForm) = {
    (for {
      action <- form.action.toOption if action.source == MARGIN
      amount <- form.amount.toOption
      marginFunds <- form.marginFunds.toOption
    } yield amount > marginFunds).contains(true)
  }

}

/**
  * Transfer Funds Dialog Controller Singleton
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object TransferFundsDialogController {

  type TransferFundsResult = Portfolio

  private val CASH = "cash"
  private val MARGIN = "margin"

  private val TransferActions = js.Array(
    new TransferFundsAction(label = "Cash to Margin Account", source = CASH),
    new TransferFundsAction(label = "Margin Account to Cash", source = MARGIN))

  /**
    * Implicit conversion from TransferFundsForm to FundsTransferForm
    * @param form the given [[TransferFundsForm form]]
    * @return the converted [[FundsTransferRequest form]]
    */
  implicit def transferFormConversion(form: TransferFundsForm): FundsTransferRequest = {
    new FundsTransferRequest(accountType = form.action.flatMap(_.source), amount = form.amount)
  }

  /**
    * Transfer Funds Form
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  class TransferFundsForm(var action: js.UndefOr[TransferFundsAction] = js.undefined,
                          var amount: js.UndefOr[Double] = js.undefined,
                          var funds: js.UndefOr[Double] = js.undefined,
                          var marginFunds: js.UndefOr[Double] = js.undefined) extends js.Object

  /**
    * Transfer Funds Action
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  class TransferFundsAction(val label: String, val source: String) extends js.Object

}

/**
  * Transfer Funds Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait TransferFundsScope extends Scope {
  // variables
  var actions: js.Array[TransferFundsAction] = js.native
  var form: TransferFundsForm = js.native

  // functions
  var init: js.Function0[Unit] = js.native
  var getMessages: js.Function0[js.Array[String]] = js.native
  var hasMessages: js.Function0[Boolean] = js.native
  var cancel: js.Function0[Unit] = js.native
  var accept: js.Function1[TransferFundsForm, Unit] = js.native

}
