package com.shocktrade.client.dialogs

import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.client.dialogs.NewsQuoteDialogController.NewsQuoteDialogResult
import com.shocktrade.client.discover.QuoteService
import org.scalajs.angularjs.AngularJsHelper.ExceptionExtensions
import org.scalajs.angularjs._
import org.scalajs.angularjs.http.Http
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import org.scalajs.dom.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * News Quote Dialog Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NewsQuoteDialog($http: Http, $modal: Modal) extends Service {

  /**
    * Popups the News Quote Dialog
    */
  def popup(symbol: String): Future[NewsQuoteDialogResult] = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[NewsQuoteDialogResult](new ModalOptions(
      templateUrl = "news_quote_dialog.html",
      controller = classOf[NewsQuoteDialogController].getSimpleName,
      resolve = js.Dictionary("symbol" -> (() => symbol))
    ))
    $modalInstance.result
  }

}

/**
  * News Quote Dialog Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NewsQuoteDialogController($scope: NewsQuoteScope, $modalInstance: ModalInstance[NewsQuoteDialogResult], toaster: Toaster,
                                @injected("NewsQuoteDialog") newsQuoteDialog: NewsQuoteDialog,
                                @injected("QuoteService") quoteService: QuoteService,
                                @injected("symbol") symbol: String)
  extends Controller {

  $scope.quote = ResearchQuote(symbol = symbol)

  $scope.init = (aSymbol: js.UndefOr[String]) => aSymbol foreach { symbol =>
    quoteService.getBasicQuote(symbol) onComplete {
      case Success(quote) => $scope.quote = quote
      case Failure(e) =>
        toaster.error(e.getMessage)
        console.error(s"Error retrieving order quote: ${e.displayMessage}")
    }
  }

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  $scope.ok = (aForm: js.UndefOr[ResearchQuote]) => aForm foreach $modalInstance.close

}

/**
  * News Quote Dialog Controller
  */
object NewsQuoteDialogController {

  type NewsQuoteDialogResult = ResearchQuote

}

/**
  * News Quote Dialog Scope
  */
@js.native
trait NewsQuoteScope extends Scope {
  // variables
  var quote: js.UndefOr[ResearchQuote]

  // functions
  var init: js.Function1[js.UndefOr[String], Unit]
  var cancel: js.Function0[Unit]
  var ok: js.Function1[js.UndefOr[ResearchQuote], Unit]

}

