package com.shocktrade.javascript.dialogs

import com.ldaniels528.scalascript.Service
import com.ldaniels528.scalascript.core.{Http, Q}
import com.ldaniels528.scalascript.extensions.{Modal, ModalOptions}

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
