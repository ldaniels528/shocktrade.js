package com.shocktrade.client.dialogs

import com.shocktrade.client.contest.PortfolioService
import com.shocktrade.client.contest.models.Portfolio
import com.shocktrade.client.dialogs.NewOrderDialog._
import com.shocktrade.client.discover.QuoteService
import com.shocktrade.client.{AutoCompletionController, AutoCompletionControllerScope}
import com.shocktrade.common.forms.{NewOrderForm, PerksResponse}
import com.shocktrade.common.models.quote.{AutoCompleteQuote, OrderQuote}
import com.shocktrade.common.models.user.UserProfile
import com.shocktrade.common.{Commissions, Ok}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
 * New Order Dialog
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewOrderDialog($uibModal: Modal) extends Service {

  /**
   * Opens a new Order Entry Pop-up Dialog
   */
  def popup(contestID: String, userID: String, params: js.UndefOr[NewOrderForm] = js.undefined): js.Promise[NewOrderDialogResult] = {
    val $uibModalInstance = $uibModal.open[NewOrderDialogResult](new ModalOptions(
      templateUrl = "new_order_dialog.html",
      controller = classOf[NewOrderDialogController].getSimpleName,
      resolve = js.Dictionary("contestID" -> (() => contestID), "userID" -> (() => userID), "params" -> (() => params))
    ))
    $uibModalInstance.result
  }

}

/**
 * New Order Dialog Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object NewOrderDialog {
  type NewOrderDialogResult = Ok

  /**
   * New Order Dialog Controller
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class NewOrderDialogController($scope: NewOrderScope, $uibModalInstance: ModalInstance[NewOrderDialogResult],
                                 $q: Q, $timeout: Timeout, toaster: Toaster,
                                 @injected("PortfolioService") portfolioService: PortfolioService,
                                 @injected("QuoteService") quoteService: QuoteService,
                                 @injected("contestID") contestID: () => String,
                                 @injected("userID") userID: () => String,
                                 @injected("params") params: () => js.UndefOr[NewOrderForm])
    extends AutoCompletionController($scope, $q, quoteService) {

    private val messages = emptyArray[String]

    $scope.isCreatingOrder = false
    $scope.form = params().getOrElse(NewOrderForm(emailNotify = true))
    $scope.quote = $scope.form.symbol.map(symbol => OrderQuote(symbol = symbol)).getOrElse(OrderQuote())

    ///////////////////////////////////////////////////////////////////////////
    //          Initialization
    ///////////////////////////////////////////////////////////////////////////

    $scope.init = () => {
      $scope.form.symbol.flat foreach lookupSymbolQuote
      loadPerks()
    }

    private def loadPerks(): js.UndefOr[js.Promise[HttpResponse[PerksResponse]]] = {
      $scope.portfolio.flatMap(_.portfolioID) map { portfolioId =>
        // load the player"s perks
        val outcome = portfolioService.findPurchasedPerks(portfolioId)
        outcome onComplete {
          case Success(contest) => $scope.form.perks = contest.data.perkCodes
          case Failure(e) =>
            toaster.error("Error retrieving perks")
            e.printStackTrace()
        }
        outcome
      }
    }

    ///////////////////////////////////////////////////////////////////////////
    //          Public Functions
    ///////////////////////////////////////////////////////////////////////////

    $scope.cancel = () => $uibModalInstance.dismiss("cancel")

    $scope.getMessages = () => messages

    $scope.placeOrder = (aForm: js.UndefOr[NewOrderForm]) => {
      messages.removeAll()
      val inputs = (for {form <- aForm} yield form).toOption
      inputs match {
        case Some(form) =>
          $scope.isCreatingOrder = true
          val outcome = for {
            messages <- validate(form)
            response <- portfolioService.createOrder(contestID(), userID(), $scope.form)
          } yield (messages, response.data)

          outcome onComplete (_ => $timeout(() => $scope.isCreatingOrder = false, 0.5.seconds))
          outcome onComplete {
            case Success((_, response)) => $uibModalInstance.close(response)
            case Success((errors, _)) => messages.push(errors: _*)
            case Failure(e) =>
              messages.push(s"The order could not be processed")
              console.error(s"order processing error: userID = $userID, contestID = $contestID, form = ${angular.toJson(form)}")
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

    private def lookupTickerQuote(aValue: js.UndefOr[Any]): Unit = aValue foreach {
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

    private def lookupSymbolQuote(symbol: String): Unit = {
      quoteService.getOrderQuote(symbol) onComplete {
        case Success(response) =>
          val quote = response.data
          $scope.$apply { () =>
            $scope.quote = quote
            $scope.form.symbol = quote.symbol
            $scope.form.limitPrice = quote.lastTrade
            $scope.form.exchange = quote.exchange
          }
        case Failure(e) =>
          messages.push(s"The order could not be processed (error code ${e.getMessage})")
      }
    }

    private def validate(form: NewOrderForm): Future[js.Array[String]] = {
      val messages = form.validate
      if (messages.isEmpty) {
        form.symbol.toOption match {
          case Some(symbol) => quoteService.getOrderQuote(symbol).map(_.data) map { quote =>
            if (quote.lastTrade.nonAssigned) messages.push(s"Current pricing could not be determined for $symbol")
            messages
          }
          case None => Future.successful(messages)
        }
      }
      else Future.successful(messages)
    }

  }

  /**
   * New Order Dialog Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait NewOrderScope extends AutoCompletionControllerScope {
    // variables
    var form: NewOrderForm = js.native
    var isCreatingOrder: js.UndefOr[Boolean] = js.native
    var portfolio: js.UndefOr[Portfolio] = js.native
    var quote: OrderQuote = js.native
    var ticker: js.Any = js.native
    var userProfile: js.UndefOr[UserProfile] = js.native

    // functions
    var init: js.Function0[js.UndefOr[js.Promise[HttpResponse[PerksResponse]]]] = js.native
    var cancel: js.Function0[Unit] = js.native
    var getMessages: js.Function0[js.Array[String]] = js.native
    var placeOrder: js.Function1[js.UndefOr[NewOrderForm], Unit] = js.native
    var onSelectedItem: js.Function3[js.UndefOr[AutoCompleteQuote], js.UndefOr[AutoCompleteQuote], js.UndefOr[String], Unit] = js.native
    var orderQuote: js.Function1[js.Dynamic, Unit] = js.native
    var getTotal: js.Function1[js.UndefOr[NewOrderForm], js.UndefOr[Double]] = js.native

  }

}