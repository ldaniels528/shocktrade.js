package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript._
import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalInstance, ModalOptions, Toaster}
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.NewsQuoteDialogController.NewsQuoteDialogResult
import com.shocktrade.javascript.models.OrderQuote
import org.scalajs.dom.console

import scala.concurrent.Future
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
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

  def getQuote(symbol: String): Future[OrderQuote] = {
    required("symbol", symbol)
    $http.get[OrderQuote](s"/api/quotes/order/symbol/$symbol")
  }
}

/**
  * News Quote Dialog Controller
  * @author lawrence.daniels@gmail.com
  */
class NewsQuoteDialogController($scope: NewsQuoteScope, $modalInstance: ModalInstance[NewsQuoteDialogResult], toaster: Toaster,
                                @injected("NewsQuoteDialog") newsQuoteDialog: NewsQuoteDialog,
                                @injected("symbol") symbol: String)
  extends Controller {

  $scope.quote = makeNew[OrderQuote]
  $scope.quote.foreach(_.symbol = symbol)

  $scope.init = (aSymbol: js.UndefOr[String]) => aSymbol foreach { symbol =>
    newsQuoteDialog.getQuote(symbol) onComplete {
      case Success(quote) => $scope.quote = quote
      case Failure(e) =>
        toaster.error(e.getMessage)
        console.error(s"Error: ${e.getMessage}")
    }
  }

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  $scope.ok = (aForm: js.UndefOr[OrderQuote]) => aForm foreach $modalInstance.close

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
@js.native
trait NewsQuoteScope extends Scope {
  // variables
  var quote: js.UndefOr[OrderQuote]

  // functions
  var init: js.Function1[js.UndefOr[String], Unit]
  var cancel: js.Function0[Unit]
  var ok: js.Function1[js.UndefOr[OrderQuote], Unit]

}

