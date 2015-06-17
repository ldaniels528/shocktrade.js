package com.shocktrade.javascript.dialogs

import biz.enef.angulate.Service
import biz.enef.angulate.core.{HttpPromise, HttpService}
import com.greencatsoft.angularjs.core.Promise
import com.greencatsoft.angularjs.extensions.{ModalOptions, ModalService}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
 * Transfer Funds Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class TransferFundsDialogService($http: HttpService, $modal: ModalService) extends Service {

  /**
   * Transfer Funds pop-up dialog
   */
  @JSExport
  def popup: js.Function0[Promise] = () => {
    val options = ModalOptions()
    options.templateUrl = "transfer_funds_dialog.htm"
    options.controller = classOf[TransferFundsDialogController].getSimpleName

    val modalInstance = $modal.open(options)
    modalInstance.result
  }

  def transferFunds: js.Function3[String, String, js.Dynamic, HttpPromise[js.Dynamic]] = (contestId: String, playerId: String, form: js.Dynamic) => {
    $http.post[js.Dynamic](s"/api/contest/$contestId/margin/$playerId", form)
  }

}
