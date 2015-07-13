package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalOptions}
import com.github.ldaniels528.scalascript.{Service, injected}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.dialogs.SignUpDialogController.SignUpDialogResult
import com.shocktrade.javascript.models.UserProfile

import scala.concurrent.ExecutionContext

/**
 * Sign-Up Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class SignUpDialogService($http: Http, $modal: Modal, @injected("MySession") mySession: MySession)
  extends Service {

  def popup()(implicit ec: ExecutionContext) = {
    val modalInstance = $modal.open[SignUpDialogResult](ModalOptions(
      templateUrl = "sign_up_dialog.htm",
      controller = classOf[SignUpDialogController].getSimpleName
    ))
    modalInstance.result
  }

  def createAccount(form: SignUpForm)(implicit ec: ExecutionContext) = $http.post[UserProfile]("/api/profile/create", form)

}

