package com.shocktrade.javascript.dialogs

import biz.enef.angulate.Service
import com.ldaniels528.javascript.angularjs.core.{Http, Modal, ModalOptions, Q}

import scala.scalajs.js

/**
 * Compose Message Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class ComposeMessageDialogService($http: Http, $modal: Modal, $q: Q) extends Service {

  /**
   * Popups the Compose Message Dialog
   */
  def popup() = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[js.Dynamic](ModalOptions(
      templateUrl = "compose_message.htm",
      controller = classOf[ComposeMessageDialogController].getSimpleName
    ))
    $modalInstance.result
  }

}
