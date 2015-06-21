package com.shocktrade.javascript.dialogs

import biz.enef.angulate.Service
import biz.enef.angulate.core.HttpService
import com.greencatsoft.angularjs.core.Promise
import com.greencatsoft.angularjs.extensions.{ModalOptions, ModalService}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS}

/**
 * News Quote Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewsQuoteDialogService($http: HttpService, $modal: ModalService) extends Service {

  /**
   * Popups the News Quote Dialog
   */
  def popup: js.Function1[String, Promise] = (symbol: String) => {
    val options = ModalOptions()
    options.templateUrl = "news_quote_dialog.htm"
    options.controller = classOf[NewsQuoteDialogController].getSimpleName
    options.resolve = js.Dictionary("symbol" -> (() => symbol))

    // create an instance of the dialog
    val $modalInstance = $modal.open(options)
    $modalInstance.result
  }

}
