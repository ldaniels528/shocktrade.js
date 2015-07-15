package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalOptions}
import com.shocktrade.javascript.dialogs.TransferFundsDialogController.TransferFundsResult
import com.shocktrade.javascript.models.Contest

import scala.concurrent.Future

/**
 * Transfer Funds Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class TransferFundsDialogService($http: Http, $modal: Modal) extends Service {

  /**
   * Transfer Funds pop-up dialog
   */
  def popup(): Future[TransferFundsResult] = {
    val modalInstance = $modal.open[TransferFundsResult](ModalOptions(
      templateUrl = "transfer_funds_dialog.htm",
      controllerClass = classOf[TransferFundsDialogController]
    ))
    modalInstance.result
  }

  def transferFunds(contestId: String, playerId: String, form: TransferFundsForm): Future[Contest] = {
    $http.post[Contest](s"/api/contest/$contestId/margin/$playerId", form)
  }

}
