package com.shocktrade.javascript.dialogs

import biz.enef.angulate.core.HttpService
import biz.enef.angulate.{Service, named}
import com.greencatsoft.angularjs.core.Promise
import com.greencatsoft.angularjs.extensions.{ModalOptions, ModalService}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}

/**
 * Sign-Up Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class SignUpDialogService($http: HttpService, $modal: ModalService, @named("MySession") mySession: MySession) extends Service {

  /**
   * Sign-up Modal Dialog
   */
  def popup(facebookID: String, fbProfile: js.Dynamic): Promise = {
    val options = ModalOptions()
    options.templateUrl = "sign_up_dialog.htm"
    options.controller = classOf[SignUpDialogController].getSimpleName

    val modalInstance = $modal.open(options)
    modalInstance.result
  }

  def createAccount(form: js.Dynamic) = {
    g.console.log(s"Creating account ${toJson(form)}")
    $http.post[js.Dynamic]("/api/profile/create", form)
  }

}
