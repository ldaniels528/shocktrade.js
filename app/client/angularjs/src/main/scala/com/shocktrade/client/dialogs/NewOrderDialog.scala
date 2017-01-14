package com.shocktrade.client.dialogs

import com.shocktrade.client.contest.PortfolioService
import com.shocktrade.client.dialogs.NewOrderDialogController.{NewOrderDialogResult, NewOrderParams}
import com.shocktrade.client.discover.QuoteService
import com.shocktrade.client.models.contest.Portfolio
import com.shocktrade.client.{AutoCompletionController, AutoCompletionControllerScope, MySessionService}
import com.shocktrade.common.Commissions
import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.common.models.quote.{AutoCompleteQuote, OrderQuote}
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.http.Http
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.npm.angularjs.{Q, Service, Timeout, angular, injected, _}
import io.scalajs.dom.html.browser.console
import io.scalajs.util.ScalaJsHelper._
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success, Try}

/**
  * New Order Dialog Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NewOrderDialog($http: Http, $modal: Modal) extends Service {

  /**
    * Opens a new Order Entry Pop-up Dialog
    */
  def popup(params: NewOrderParams) = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[NewOrderDialogResult](new ModalOptions(
      templateUrl = "new_order_dialog.html",
      controller = classOf[NewOrderDialogController].getSimpleName,
      resolve = js.Dictionary("params" -> (() => params))
    ))
    $modalInstance.result
  }

}

/**
  * New Order Dialog Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NewOrderDialogController($scope: NewOrderScope, $modalInstance: ModalInstance[NewOrderDialogResult],
                               $q: Q, $timeout: Timeout, toaster: Toaster,
                               @injected("MySessionService") mySession: MySessionService,
                               @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
                               @injected("PerksDialog") perksDialog: PerksDialog,
                               @injected("PortfolioService") portfolioService: PortfolioService,
                               @injected("QuoteService") quoteService: QuoteService,
                               @injected("params") params: NewOrderParams)
  extends AutoCompletionController($scope, $q, quoteService) {

  private val messages = emptyArray[String]
  private var processing = false

  $scope.form = new NewOrderForm(
    symbol = params.symbol,
    quantity = params.quantity,
    accountType = params.accountType,
    emailNotify = true
  )

  $scope.quote = $scope.form.symbol.toOption match {
    case Some(symbol) => OrderQuote(symbol = symbol)
    case None => OrderQuote()
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.init = () => $scope.form.symbol foreach lookupSymbolQuote

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  $scope.getMessages = () => messages

  $scope.isProcessing = () => processing

  $scope.ok = (aForm: js.UndefOr[NewOrderForm]) => aForm foreach { form =>
    messages.removeAll()
    val inputs = for {
      portfolioId <- mySession.portfolio_?.flatMap(_._id.toOption)
      playerId <- mySession.userProfile._id.toOption
    } yield (portfolioId, playerId)

    inputs match {
      case Some((portfolioId, playerId)) =>
        processing = true
        val outcome = for {
          messages <- validate(form)
          portfolio_? <- if (messages.isEmpty) portfolioService.createOrder(portfolioId, $scope.form).map(Option(_)) else Future.successful(None)
        } yield (messages, portfolio_?)

        outcome onComplete {
          case Success((_, Some(portfolio))) =>
            $timeout(() => processing = false, 0.5.seconds)
            $modalInstance.close(portfolio)
          case Success((errors, _)) =>
            $timeout(() => processing = false, 0.5.seconds)
            messages.push(errors: _*)
          case Failure(e) =>
            $timeout(() => processing = false, 0.5.seconds)
            messages.push(s"The order could not be processed")
            console.error(s"order processing error: portfolioId = $portfolioId, playerId = $playerId, form = ${angular.toJson(form)}")
            e.printStackTrace()
        }
      case None =>
        toaster.error("User session error")
    }
  }

  $scope.onSelectedItem = (aItem: js.UndefOr[AutoCompleteQuote], aModel: js.UndefOr[AutoCompleteQuote], aLabel: js.UndefOr[String]) => {
    aModel.flatMap(_.symbol) foreach { symbol =>
      console.log(s"Loading '$symbol' => ${angular.toJson(aModel)}")
      $scope.ticker = symbol
      lookupSymbolQuote(symbol)
    }
  }

  $scope.orderQuote = (ticker: js.Dynamic) => lookupTickerQuote(ticker)

  $scope.getTotal = (aForm: js.UndefOr[NewOrderForm]) => aForm flatMap { form =>
    for {
      price <- form.limitPrice
      quantity <- form.quantity
    } yield price * quantity + Commissions(form)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def lookupTickerQuote(aValue: js.UndefOr[Any]) = aValue foreach {
    case ticker: String =>
      lookupSymbolQuote(ticker.indexOf(" ") match {
        case -1 => ticker
        case index => ticker.substring(0, index)
      })

    case unknown =>
      Try(unknown.asInstanceOf[AutoCompleteQuote]) match {
        case Success(quote) =>
          console.log(s"Loading symbol from ${angular.toJson(quote, pretty = false)}")
          quote.symbol foreach lookupSymbolQuote
        case Failure(e) =>
          console.error(s"Unhandled value '$unknown': ${e.displayMessage}")
      }
  }

  private def lookupSymbolQuote(symbol: String) = {
    quoteService.getOrderQuote(symbol) onComplete {
      case Success(quote) =>
        $scope.quote = quote
        $scope.form.symbol = quote.symbol
        $scope.form.limitPrice = quote.lastTrade
        $scope.form.exchange = quote.exchange
      case Failure(e) =>
        messages.push(s"The order could not be processed (error code ${e.getMessage})")
    }
  }

  private def validate(form: NewOrderForm) = {
    val messages = form.validate
    if (form.isMarginAccount && mySession.marginAccount_?.isEmpty) messages.push("You do not have a Margin Account (must buy the Perk)")
    if (messages.isEmpty) {
      form.symbol.toOption match {
        case Some(symbol) => quoteService.getOrderQuote(symbol) map { quote =>
          if (quote.lastTrade.nonAssigned) messages.push(s"Current pricing could not be determined for $symbol")
          messages
        }
        case None => Future.successful(messages)
      }
    }
    else Future.successful(messages)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  mySession.portfolio_?.flatMap(_._id.toOption) foreach { portfolioId =>
    // load the player"s perks
    perksDialog.getMyPerkCodes(portfolioId) onComplete {
      case Success(contest) => $scope.form.perks = contest.perkCodes
      case Failure(e) =>
        toaster.error("Error retrieving perks")
    }
  }

}

/**
  * New Order Dialog Controller Singleton
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object NewOrderDialogController {

  type NewOrderDialogResult = Portfolio

  /**
    * New Order Dialog Parameters
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @ScalaJSDefined
  class NewOrderParams(val symbol: js.UndefOr[String] = js.undefined,
                       val accountType: js.UndefOr[String] = js.undefined,
                       val quantity: js.UndefOr[Double] = js.undefined) extends js.Object

}

/**
  * New Order Dialog Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait NewOrderScope extends AutoCompletionControllerScope {
  // variables
  var form: NewOrderForm = js.native
  var quote: OrderQuote = js.native
  var ticker: js.Any = js.native

  // functions
  var init: js.Function0[Unit] = js.native
  var cancel: js.Function0[Unit] = js.native
  var getMessages: js.Function0[js.Array[String]] = js.native
  var isProcessing: js.Function0[Boolean] = js.native
  var ok: js.Function1[js.UndefOr[NewOrderForm], Unit] = js.native
  var onSelectedItem: js.Function3[js.UndefOr[AutoCompleteQuote], js.UndefOr[AutoCompleteQuote], js.UndefOr[String], Unit] = js.native
  var orderQuote: js.Function1[js.Dynamic, Unit] = js.native
  var getTotal: js.Function1[js.UndefOr[NewOrderForm], js.UndefOr[Double]] = js.native

}