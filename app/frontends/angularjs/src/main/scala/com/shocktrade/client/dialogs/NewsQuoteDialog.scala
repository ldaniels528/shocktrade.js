package com.shocktrade.client.dialogs

import com.shocktrade.client.dialogs.NewsQuoteDialogController.NewsQuoteDialogResult
import com.shocktrade.client.discover.QuoteService
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper.ExceptionExtensions
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * News Quote Dialog Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewsQuoteDialog($uibModal: Modal) extends Service {

  /**
   * Popups the News Quote Dialog
   */
  def popup(symbol: String): js.Promise[NewsQuoteDialogResult] = {
    val $uibModalInstance = $uibModal.open[NewsQuoteDialogResult](new ModalOptions(
      templateUrl = "news_quote_dialog.html",
      controller = classOf[NewsQuoteDialogController].getSimpleName,
      resolve = js.Dictionary("symbol" -> (() => symbol))
    ))
    $uibModalInstance.result
  }

}

/**
 * News Quote Dialog Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewsQuoteDialogController($scope: NewsQuoteScope, $uibModalInstance: ModalInstance[NewsQuoteDialogResult], toaster: Toaster,
                                @injected("NewsQuoteDialog") newsQuoteDialog: NewsQuoteDialog,
                                @injected("QuoteService") quoteService: QuoteService,
                                @injected("symbol") symbol: => String)
  extends Controller {

  $scope.quote = js.Dictionary("symbol" -> symbol)

  $scope.init = (aSymbol: js.UndefOr[String]) => aSymbol foreach { symbol =>
    quoteService.getBasicQuote(symbol) onComplete {
      case Success(quote) => $scope.quote = quote.data.asInstanceOf[NewsQuoteDialogResult]
      case Failure(e) =>
        toaster.error(e.getMessage)
        console.error(s"Error retrieving order quote: ${e.displayMessage}")
    }
  }

  $scope.cancel = () => $uibModalInstance.dismiss("cancel")

  $scope.ok = (aForm: js.UndefOr[NewsQuoteDialogResult]) => aForm foreach $uibModalInstance.close

}

/**
 * News Quote Dialog Controller
 */
object NewsQuoteDialogController {

  type NewsQuoteDialogResult = js.Dictionary[String]

}

/**
 * News Quote Dialog Scope
 */
@js.native
trait NewsQuoteScope extends Scope {
  // variables
  var quote: js.UndefOr[NewsQuoteDialogResult]

  // functions
  var init: js.Function1[js.UndefOr[String], Unit]
  var cancel: js.Function0[Unit]
  var ok: js.Function1[js.UndefOr[NewsQuoteDialogResult], Unit]

}

