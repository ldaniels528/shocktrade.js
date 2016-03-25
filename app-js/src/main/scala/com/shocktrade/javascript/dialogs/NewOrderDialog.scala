package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.core.TimerConversions._
import com.github.ldaniels528.scalascript.core.{Http, Q, Timeout}
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalInstance, ModalOptions, Toaster}
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.github.ldaniels528.scalascript.{Service, angular, injected}
import com.shocktrade.javascript.dialogs.NewOrderDialogController.NewOrderDialogResult
import com.shocktrade.javascript.discover.QuoteService
import com.shocktrade.javascript.models.{BSONObjectID, Contest, OrderQuote}
import com.shocktrade.javascript.{AutoCompletionController, AutoCompletionControllerScope, MySessionService}
import org.scalajs.dom.console

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
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
  def popup(params: NewOrderParams) = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[NewOrderDialogResult](ModalOptions(
      templateUrl = "new_order_dialog.htm",
      controllerClass = classOf[NewOrderDialogController],
      resolve = js.Dictionary("params" -> (() => params))
    ))
    $modalInstance.result
  }

  def createOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: NewOrderForm): Future[Contest] = {
    required("contestId", contestId)
    required("playerId", playerId)
    required("order", order)
    $http.put[Contest](s"/api/order/${contestId.$oid}/${playerId.$oid}", order)
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
                               @injected("MySessionService") mySession: MySessionService,
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

  $scope.init = () => $scope.form.symbol foreach lookupSymbolQuote

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  $scope.getMessages = () => messages

  $scope.isProcessing = () => processing

  $scope.ok = (aForm: js.UndefOr[NewOrderForm]) => aForm foreach { form =>
    if (isValid(form)) {
      val outcome = for {
        playerId <- mySession.userProfile._id.toOption
        contestId <- mySession.contest.flatMap(_._id.toOption)
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

  $scope.orderQuote = (ticker: js.Dynamic) => lookupTickerQuote(ticker)

  $scope.getTotal = (aForm: js.UndefOr[NewOrderForm]) => aForm map { form =>
    form.limitPrice.getOrElse(0d) * form.quantity.getOrElse(0d)
  }

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
    contestId <- mySession.contest.flatMap(_._id.toOption)
    playerId <- mySession.userProfile._id.toOption
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
  * New Order Dialog Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewOrderScope extends AutoCompletionControllerScope {
  // variables
  var form: NewOrderForm
  var quote: OrderQuote

  // functions
  var init: js.Function0[Unit]
  var cancel: js.Function0[Unit]
  var getMessages: js.Function0[js.Array[String]]
  var isProcessing: js.Function0[Boolean]
  var ok: js.Function1[js.UndefOr[NewOrderForm], Unit]
  var orderQuote: js.Function1[js.Dynamic, Unit]
  var getTotal: js.Function1[js.UndefOr[NewOrderForm], js.UndefOr[Double]]

}

/**
  * New Order Dialog Controller Singleton
  * @author lawrence.daniels@gmail.com
  */
object NewOrderDialogController {

  type NewOrderDialogResult = Contest
}

/**
  * New Order Dialog Form
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewOrderForm extends js.Object {
  var symbol: js.UndefOr[String]
  var exchange: js.UndefOr[String]
  var accountType: js.UndefOr[String]
  var orderType: js.UndefOr[String]
  var orderTerm: js.UndefOr[String]
  var priceType: js.UndefOr[String]
  var quantity: js.UndefOr[Double]
  var limitPrice: js.UndefOr[Double]
  var perks: js.Array[String]
  var emailNotify: js.UndefOr[Boolean]
}

/**
  * New Order Dialog Parameters
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewOrderParams extends js.Object {
  var accountType: js.UndefOr[String]
  var symbol: js.UndefOr[String]
  var quantity: js.UndefOr[Double]
}

/**
  * New Order Dialog Parameters Singleton
  * @author lawrence.daniels@gmail.com
  */
object NewOrderParams {

  def apply(accountType: js.UndefOr[String] = js.undefined,
            symbol: js.UndefOr[String] = js.undefined,
            quantity: js.UndefOr[Double] = js.undefined) = {
    val params = makeNew[NewOrderParams]
    params.accountType = accountType
    params.symbol = symbol
    params.quantity = quantity
    params
  }
}
