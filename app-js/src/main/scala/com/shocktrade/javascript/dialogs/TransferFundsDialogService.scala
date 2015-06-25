package com.shocktrade.javascript.dialogs

import biz.enef.angulate.Service
import com.ldaniels528.javascript.angularjs.core.{Http, Modal, ModalOptions}

import scala.scalajs.js

/**
 * Transfer Funds Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class TransferFundsDialogService($http: Http, $modal: Modal) extends Service {

  /**
   * Transfer Funds pop-up dialog
   */
  def popup() = {
    val modalInstance = $modal.open[js.Dynamic](ModalOptions(
      templateUrl = "transfer_funds_dialog.htm",
      controller = classOf[TransferFundsDialogController].getSimpleName
    ))
    modalInstance.result
  }

  def transferFunds(contestId: String, playerId: String, form: js.Dynamic) = {
    $http.post[js.Dynamic](s"/api/contest/$contestId/margin/$playerId", form)
  }

}
