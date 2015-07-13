package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.core.Q
import com.github.ldaniels528.scalascript.extensions.{ModalInstance, Toaster}
import com.github.ldaniels528.scalascript.{angular, injected, scoped}
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dashboard.OrderQuote
import com.shocktrade.javascript.dialogs.NewOrderDialogController.NewOrderDialogResult
import com.shocktrade.javascript.discover.QuoteService
import com.shocktrade.javascript.models.Contest
import com.shocktrade.javascript.{AutoCompletionController, MySession}
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * New Order Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class NewOrderDialogController($scope: NewOrderScope, $modalInstance: ModalInstance[NewOrderDialogResult],
                               $q: Q, toaster: Toaster,
                               @injected("MySession") mySession: MySession,
                               @injected("NewOrderDialog") newOrderDialog: NewOrderDialogService,
                               @injected("PerksDialog") perksDialog: PerksDialogService,
                               @injected("QuoteService") quoteService: QuoteService,
                               @injected("params") params: NewOrderParams)
  extends AutoCompletionController($scope, $q, quoteService) {

  private val messages = emptyArray[String]
  private var processing = false

  $scope.form = {
    val form = makeNew[NewOrderForm]
    form.emailNotify = true
    form.accountType = params.accountType
    form.symbol = params.symbol
    form.quantity = params.quantity
    form
  }

  $scope.quote = {
    val quote = makeNew[OrderQuote]
    quote.symbol = $scope.form.symbol.orNull
    quote
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped def init() = $scope.form.symbol foreach lookupQuote

  @scoped def cancel() = $modalInstance.dismiss("cancel")

  @scoped def getMessages = messages

  @scoped def isProcessing = processing

  @scoped def ok(form: NewOrderForm) = accept(form)

  @scoped def orderQuote(ticker: js.UndefOr[String]) = ticker foreach lookupQuote

  @scoped def getTotal(form: NewOrderForm) = form.limitPrice.getOrElse(0d) * form.quantity.getOrElse(0)

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def lookupQuote(ticker: String) = {
    if (ticker.nonBlank) {
      val symbol = (ticker.indexOfOpt(" ") map (index => ticker.substring(0, index - 1)) getOrElse ticker).trim
      newOrderDialog.lookupQuote(symbol) onComplete {
        case Success(quote) =>
          $scope.quote = quote
          $scope.form.symbol = quote.symbol
          $scope.form.limitPrice = quote.lastTrade
          $scope.form.exchange = quote.exchange
        case Failure(e) =>
          messages.push(s"The order could not be processed (error code ${e.getMessage})")
      }
    }
  }

  private def accept(form: NewOrderForm) = {
    if (isValid(form)) {
      processing = true

      val contestId = mySession.getContestID
      val playerId = mySession.getUserID
      console.log(s"contestId = $contestId, playerId = $playerId, form = ${angular.toJson(form)}")

      newOrderDialog.createOrder(contestId, playerId, $scope.form) onComplete {
        case Success(contest) =>
          processing = false
          $modalInstance.close(contest)
        case Failure(e) =>
          processing = false
          messages.push(s"The order could not be processed (error code ${e.getMessage})")
      }
    }
  }

  private def isValid(form: NewOrderForm) = {
    messages.remove(0, messages.length)

    // perform the validations
    if (!isDefined(form.accountType)) messages.push("Please selected the account to use (Cash or Margin)")
    if (isDefined(form.accountType) && form.accountType.toOption.contains("MARGIN") && mySession.marginAccount_?.isEmpty) {
      messages.push("You do not have a Margin Account (must buy the Perk)")
    }
    if (!isDefined(form.orderType)) messages.push("No Order Type (BUY or SELL) specified")
    if (!isDefined(form.priceType)) messages.push("No Pricing Method specified")
    if (!isDefined(form.orderTerm)) messages.push("No Order Term specified")
    if (!isDefined(form.quantity) || form.quantity.exists(_ == 0d)) messages.push("No quantity specified")
    messages.isEmpty
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  for {
    contestId <- Option(mySession.getContestID)
    playerId <- Option(mySession.getUserID)
  } {
    // load the player"s perks
    perksDialog.getMyPerks(contestId, playerId) onComplete {
      case Success(contest) => $scope.form.perks = contest.perkCodes
      case Failure(e) =>
        toaster.error("Error retrieving perks")
    }
  }

}

/**
 * New Order Dialog Controller Singleton
 */
object NewOrderDialogController {

  type NewOrderDialogResult = Contest
}

/**
 * New Order Dialog Form
 */
trait NewOrderForm extends js.Object {
  var symbol: js.UndefOr[String] = js.native
  var exchange: js.UndefOr[String] = js.native
  var accountType: js.UndefOr[String] = js.native
  var orderType: js.UndefOr[String] = js.native
  var orderTerm: js.UndefOr[String] = js.native
  var priceType: js.UndefOr[String] = js.native
  var quantity: js.UndefOr[Int] = js.native
  var limitPrice: js.UndefOr[Double] = js.native
  var perks: js.Array[String] = js.native
  var emailNotify: js.UndefOr[Boolean] = js.native
}

/**
 * New Order Dialog Parameters
 */
trait NewOrderParams extends js.Object {
  var accountType: js.UndefOr[String] = js.native
  var symbol: js.UndefOr[String] = js.native
  var quantity: js.UndefOr[Int] = js.native
}

/**
 * New Order Dialog Parameters Singleton
 */
object NewOrderParams {

  def apply(accountType: js.UndefOr[String] = js.undefined,
            symbol: js.UndefOr[String] = js.undefined,
            quantity: js.UndefOr[Int] = js.undefined) = {
    val params = makeNew[NewOrderParams]
    params.accountType = accountType
    params.symbol = symbol
    params.quantity = quantity
    params
  }
}

/**
 * New Order Dialog Scope
 */
trait NewOrderScope extends js.Object {
  var form: NewOrderForm = js.native
  var quote: OrderQuote = js.native
}