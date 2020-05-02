package com.shocktrade.client.dialogs

import com.shocktrade.client.dialogs.StockQuoteDialogController.{StockQuoteDialogResult, StockQuoteDialogScope}
import com.shocktrade.client.discover.QuoteService
import com.shocktrade.common.models.quote.CompleteQuote
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.http.HttpResponse
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
 * Stock Dialog Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class StockQuoteDialogController($scope: StockQuoteDialogScope, $timeout: Timeout,
                                      $uibModalInstance: ModalInstance[StockQuoteDialogResult],
                                      @injected("QuoteService") quoteService: QuoteService,
                                      @injected("symbol") symbol: () => String) extends Controller {

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

/**
 * Stock Dialog Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object StockQuoteDialogController {
  type StockQuoteDialogResult = CompleteQuote

  @js.native
  trait StockQuoteDialogScope extends Scope {
    // functions
    var cancel: js.Function0[Unit] = js.native
    var dismiss: js.Function1[js.Any, Unit] = js.native
    var init: js.Function0[js.Promise[HttpResponse[CompleteQuote]]] = js.native

    // variables
    var q: js.UndefOr[CompleteQuote] = js.native
  }

}