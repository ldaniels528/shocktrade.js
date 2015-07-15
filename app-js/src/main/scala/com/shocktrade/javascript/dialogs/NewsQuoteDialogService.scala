package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalOptions}
import com.shocktrade.javascript.dialogs.NewsQuoteDialogController.NewsQuoteDialogResult

import scala.concurrent.Future
import scala.scalajs.js

/**
 * News Quote Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewsQuoteDialogService($http: Http, $modal: Modal) extends Service {

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
