package com.shocktrade.client.dialogs

import com.shocktrade.client.GameStateService
import com.shocktrade.client.contest.PortfolioService
import com.shocktrade.client.dialogs.StockQuoteDialog.{StockQuoteDialogController, StockQuoteDialogResult}
import com.shocktrade.client.discover.QuoteService
import com.shocktrade.client.users.{PersonalSymbolSupport, PersonalSymbolSupportScope}
import com.shocktrade.common.models.quote.CompleteQuote
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Stock Quote Dialog Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class StockQuoteDialog($uibModal: Modal) extends Service {

  def lookupSymbol(symbol: String): js.Promise[StockQuoteDialogResult] = {
    val modalInstance = $uibModal.open[StockQuoteDialogResult](new ModalOptions(
      controller = classOf[StockQuoteDialogController].getSimpleName,
      resolve = js.Dictionary("symbol" -> (() => symbol)),
      templateUrl = "stock_quote_dialog.html"
    ))
    modalInstance.result
  }

}

/**
 * Stock Quote Dialog Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object StockQuoteDialog {
  type StockQuoteDialogResult = CompleteQuote

  /**
   * Stock Dialog Controller
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  case class StockQuoteDialogController($scope: StockQuoteDialogScope, $q: Q, $timeout: Timeout, toaster: Toaster,
                                        $uibModalInstance: ModalInstance[StockQuoteDialogResult],
                                        @injected("GameStateService") gameStateService: GameStateService,
                                        @injected("PortfolioService") portfolioService: PortfolioService,
                                        @injected("QuoteService") quoteService: QuoteService,
                                        @injected("symbol") symbol: () => String)
    extends Controller with PersonalSymbolSupport {

    $scope.cancel = () => $uibModalInstance.dismiss("cancel")

    $scope.dismiss = (form: js.Any) => $uibModalInstance.dismiss("cancel")

    $scope.init = () => {
      console.info(s"Initializing ${getClass.getSimpleName}...")
      val outcome = quoteService.getCompleteQuote(symbol())
      outcome onComplete {
        case Success(quote) => $scope.$apply(() => $scope.q = quote.data)
        case Failure(e) =>
          e.printStackTrace()
      }
      outcome
    }

  }

  @js.native
  trait StockQuoteDialogScope extends Scope with PersonalSymbolSupportScope {
    // functions
    var cancel: js.Function0[Unit] = js.native
    var dismiss: js.Function1[js.Any, Unit] = js.native
    var init: js.Function0[js.Promise[HttpResponse[CompleteQuote]]] = js.native

    // variables
    var q: js.UndefOr[CompleteQuote] = js.native
  }

  /**
   * Stock Quote Dialog Support
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  trait StockQuoteDialogSupport {
    ref: Controller =>

    def $scope: StockQuoteDialogSupportScope

    def stockQuoteDialog: StockQuoteDialog

    $scope.stockQuotePopup = (aSymbol: js.UndefOr[String]) => aSymbol map stockQuoteDialog.lookupSymbol

  }

  /**
   * Stock Quote Dialog Support Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait StockQuoteDialogSupportScope extends Scope {
    // functions
    var stockQuotePopup: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[StockQuoteDialogResult]]] = js.native

  }

}