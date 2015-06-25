package com.shocktrade.javascript.dialogs

import biz.enef.angulate.{Service, named}
import com.ldaniels528.javascript.angularjs.core.{Http, Modal, ModalOptions}
import com.shocktrade.javascript.MySession

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

/**
 * Sign-Up Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class SignUpDialogService($http: Http, $modal: Modal, @named("MySession") mySession: MySession) extends Service {

  /**
   * Sign-up Modal Dialog
   */
  def popup(facebookID: String, fbProfile: js.Dynamic)(implicit ec: ExecutionContext) = {
    val modalInstance = $modal.open(ModalOptions(
      templateUrl = "sign_up_dialog.htm",
      controller = classOf[SignUpDialogController].getSimpleName
    ))
    modalInstance.result
  }

  def createAccount(form: js.Dynamic)(implicit ec: ExecutionContext) = $http.post[js.Dynamic]("/api/profile/create", form)

}
