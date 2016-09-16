package com.shocktrade.stockguru.dialogs

import com.shocktrade.Commissions
import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.common.models.contest.Portfolio
import com.shocktrade.common.models.quote.{AutoCompleteQuote, ResearchQuote}
import com.shocktrade.stockguru.contest.PortfolioService
import com.shocktrade.stockguru.dialogs.NewOrderDialogController.{NewOrderDialogResult, NewOrderParams}
import com.shocktrade.stockguru.discover.QuoteService
import com.shocktrade.stockguru.{AutoCompletionController, AutoCompletionControllerScope, MySessionService}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs.http.Http
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import org.scalajs.angularjs.{Q, Service, Timeout, angular, injected, _}
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._

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

  $scope.quote = new ResearchQuote(symbol = $scope.form.symbol)

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
        contestId <- mySession.contest_?.flatMap(_._id.toOption)
      } yield (playerId, contestId)

      outcome match {
        case Some((playerId, contestId)) =>
          processing = true
          portfolioService.createOrder(contestId, playerId, $scope.form) onComplete {
            case Success(portfolio) =>
              $timeout(() => processing = false, 0.5.seconds)
              $modalInstance.close(portfolio)
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
    quoteService.getBasicQuote(symbol) onComplete {
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
  var quote: ResearchQuote = js.native
  var ticker: js.Any

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