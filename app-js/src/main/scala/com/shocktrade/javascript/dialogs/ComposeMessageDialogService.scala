package com.shocktrade.javascript.dialogs

import biz.enef.angulate.Service
import biz.enef.angulate.core.HttpService
import com.greencatsoft.angularjs.core.{Promise, Q}
import com.greencatsoft.angularjs.extensions.{ModalOptions, ModalService}

import scala.scalajs.js

/**
 * Compose Message Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class ComposeMessageDialogService($http: HttpService, $modal: ModalService, $q: Q) extends Service {

  /**
   * Popups the Compose Message Dialog
   */
  def popup: js.Function0[Promise] = () => {
    // create an instance of the dialog
    val options = ModalOptions()
    options.templateUrl = "compose_message.htm"
    options.controller = classOf[ComposeMessageDialogController].getSimpleName

    val $modalInstance = $modal.open(options)
    $modalInstance.result
  }

}
