package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalInstance, ModalOptions, Toaster}
import com.github.ldaniels528.scalascript.{Controller, Scope, Service, injected, scoped}
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.dialogs.NewsQuoteDialogController.NewsQuoteDialogResult
import com.shocktrade.javascript.models.OrderQuote

import scala.concurrent.Future
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Failure, Success}

/**
 * News Quote Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewsQuoteDialog($http: Http, $modal: Modal) extends Service {

  /**
   * Popups the News Quote Dialog
   */
  def popup(symbol: String): Future[NewsQuoteDialogResult] = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[NewsQuoteDialogResult](ModalOptions(
      templateUrl = "news_quote_dialog.htm",
      controllerClass = classOf[NewsQuoteDialogController],
      resolve = js.Dictionary("symbol" -> (() => symbol))
    ))
    $modalInstance.result
  }
}

/**
 * News Quote Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class NewsQuoteDialogController($scope: NewsQuoteScope, $modalInstance: ModalInstance[NewsQuoteDialogResult],
                                toaster: Toaster,
                                @injected("ContestService") contestService: ContestService,
                                @injected("symbol") symbol: String)
  extends Controller {

  $scope.quote = makeNew[OrderQuote]
  $scope.quote.symbol = symbol

  @scoped def init(symbol: String) {
    contestService.orderQuote(symbol) onComplete {
      case Success(quote) => $scope.quote = quote
      case Failure(e) =>
        toaster.error(e.getMessage)
        g.console.error(s"Error: ${e.getMessage}")
    }
  }

  @scoped def cancel() = $modalInstance.dismiss("cancel")

  @scoped def ok(form: OrderQuote) = $modalInstance.close(form)

}

/**
 * News Quote Dialog Controller
 */
object NewsQuoteDialogController {

  type NewsQuoteDialogResult = OrderQuote
}

/**
 * News Quote Dialog Scope
 */
trait NewsQuoteScope extends Scope {
  var quote: OrderQuote = js.native
}

