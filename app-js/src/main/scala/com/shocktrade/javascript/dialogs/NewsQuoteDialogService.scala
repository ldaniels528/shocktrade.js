package com.shocktrade.javascript.dialogs

import biz.enef.angulate.Service
import com.ldaniels528.javascript.angularjs.core.{Http, Modal, ModalOptions}

import scala.scalajs.js

/**
 * News Quote Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewsQuoteDialogService($http: Http, $modal: Modal) extends Service {

  /**
   * Popups the News Quote Dialog
   */
  def popup(symbol: String) = {
    // create an instance of the dialog
    val $modalInstance = $modal.open(ModalOptions(
      templateUrl = "news_quote_dialog.htm",
      controller = classOf[NewsQuoteDialogController].getSimpleName,
      resolve = js.Dictionary("symbol" -> (() => symbol))
    ))
    $modalInstance.result
  }

}
