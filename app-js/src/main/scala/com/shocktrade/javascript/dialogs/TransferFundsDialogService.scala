package com.shocktrade.javascript.dialogs

import com.ldaniels528.scalascript.Service
import com.ldaniels528.scalascript.core.Http
import com.ldaniels528.scalascript.extensions.{ModalOptions, Modal}
import com.shocktrade.javascript.models.Contest

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
    val modalInstance = $modal.open[Contest](ModalOptions(
      templateUrl = "transfer_funds_dialog.htm",
      controller = classOf[TransferFundsDialogController].getSimpleName
    ))
    modalInstance.result
  }

  def transferFunds(contestId: String, playerId: String, form: js.Dynamic) = {
    $http.post[js.Dynamic](s"/api/contest/$contestId/margin/$playerId", form)
  }

}
