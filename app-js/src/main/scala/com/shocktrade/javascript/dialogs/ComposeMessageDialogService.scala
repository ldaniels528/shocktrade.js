package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.{Http, Q}
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalOptions}
import com.shocktrade.javascript.dialogs.ComposeMessageDialogController.ComposeMessageResult

import scala.concurrent.Future

/**
 * Compose Message Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class ComposeMessageDialogService($http: Http, $modal: Modal, $q: Q) extends Service {

  def popup(): Future[ComposeMessageResult] = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[ComposeMessageResult](ModalOptions(
      templateUrl = "compose_message.htm",
      controllerClass = classOf[ComposeMessageDialogController]
    ))
    $modalInstance.result
  }

}