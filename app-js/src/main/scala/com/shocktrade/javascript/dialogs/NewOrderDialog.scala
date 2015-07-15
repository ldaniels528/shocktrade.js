package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.core.TimerConversions._
import com.github.ldaniels528.scalascript.core.{Http, Q, Timeout}
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalInstance, ModalOptions, Toaster}
import com.github.ldaniels528.scalascript.{Service, angular, injected, scoped}
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.NewOrderDialogController.NewOrderDialogResult
import com.shocktrade.javascript.discover.QuoteService
import com.shocktrade.javascript.models.{Contest, OrderQuote}
import com.shocktrade.javascript.{AutoCompletionController, MySession}
import org.scalajs.dom.console

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * New Order Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewOrderDialog($http: Http, $modal: Modal) extends Service {

  /**
   * Opens a new Order Entry Pop-up Dialog
   */
  def popup(params: NewOrderParams): Future[NewOrderDialogResult] = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[NewOrderDialogResult](ModalOptions(
      templateUrl = "new_order_dialog.htm",
      controllerClass = classOf[NewOrderDialogController],
      resolve = js.Dictionary("params" -> (() => params))
    ))
    $modalInstance.result
  }

  def createOrder(contestId: String, playerId: String, order: NewOrderForm): Future[Contest] = {
    required("contestId", contestId)
    required("playerId", playerId)
    required("order", order)
    $http.put[Contest](s"/api/order/$contestId/$playerId", order)
  }

  def getQuote(symbol: String): Future[OrderQuote] = {
    required("symbol", symbol)
    $http.get[OrderQuote](s"/api/quotes/order/symbol/$symbol")
  }
}

/**
 * New Order Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class NewOrderDialogController($scope: NewOrderScope, $modalInstance: ModalInstance[NewOrderDialogResult],
                               $q: Q, $timeout: Timeout, toaster: Toaster,
                               @injected("MySession") mySession: MySession,
                               @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
                               @injected("PerksDialog") perksDialog: PerksDialog,
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
    quote.symbol = $scope.form.symbol
    quote
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped def init() = $scope.form.symbol foreach lookupSymbolQuote

  @scoped def cancel() = $modalInstance.dismiss("cancel")

  @scoped def getMessages = messages

  @scoped def isProcessing = processing

  @scoped def ok(form: NewOrderForm) = accept(form)

  @scoped def orderQuote(ticker: js.Dynamic) = lookupTickerQuote(ticker)

  @scoped def getTotal(form: NewOrderForm) = form.limitPrice.getOrElse(0d) * form.quantity.getOrElse(0)

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def lookupTickerQuote(ticker: js.Dynamic) = {
    console.log(s"ticker = ${angular.toJson(ticker)}")
    val _ticker = if (isDefined(ticker.symbol)) ticker.symbol.as[String] else ticker.as[String]
    if (_ticker.nonBlank) {
      val symbol = (_ticker.indexOfOpt(" ") map (index => _ticker.substring(0, index - 1)) getOrElse _ticker).trim
      lookupSymbolQuote(symbol)
    }
  }

  private def lookupSymbolQuote(symbol: String) = {
    newOrderDialog.getQuote(symbol) onComplete {
      case Success(quote) =>
        $scope.quote = quote
        $scope.form.symbol = quote.symbol
        $scope.form.limitPrice = quote.lastTrade
        $scope.form.exchange = quote.exchange
      case Failure(e) =>
        messages.push(s"The order could not be processed (error code ${e.getMessage})")
    }
  }

  private def accept(form: NewOrderForm) {
    if (isValid(form)) {
      val outcome = for {
        playerId <- mySession.userProfile.OID_?
        contestId <- mySession.contest.flatMap(_.OID_?)
      } yield (playerId, contestId)

      outcome match {
        case Some((playerId, contestId)) =>
          processing = true
          newOrderDialog.createOrder(contestId, playerId, $scope.form) onComplete {
            case Success(contest) =>
              $timeout(() => processing = false, 0.5.seconds)
              $modalInstance.close(contest)
            case Failure(e) =>
              $timeout(() => processing = false, 0.5.seconds)
              messages.push(s"The order could not be processed")
              console.error(s"order processing error: contestId = $contestId, playerId = $playerId, form = ${angular.toJson(form)}")
              e.printStackTrace()
          }
        case None =>
          toaster.error("User session error")
      }
    }
  }

  private def isValid(form: NewOrderForm) = {
    messages.removeAll()

    // perform the validations
    if (!isDefined(form.accountType)) messages.push("Please selected the account to use (Cash or Margin)")
    if (isDefined(form.accountType) && form.accountType.toOption.contains("MARGIN") && mySession.marginAccount_?.isEmpty) {
      messages.push("You do not have a Margin Account (must buy the Perk)")
    }
    if (!isDefined(form.orderType)) messages.push("No Order Type (BUY or SELL) specified")
    if (!isDefined(form.priceType)) messages.push("No Pricing Method specified")
    if (!isDefined(form.orderTerm)) messages.push("No Order Term specified")
    if (!isDefined(form.quantity) || form.quantity.exists(_ == 0)) messages.push("No quantity specified")
    messages.isEmpty
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  for {
    contestId <- mySession.contest.flatMap(_.OID_?)
    playerId <- mySession.userProfile.OID_?
  } {
    // load the player"s perks
    perksDialog.getMyPerkCodes(contestId, playerId) onComplete {
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
