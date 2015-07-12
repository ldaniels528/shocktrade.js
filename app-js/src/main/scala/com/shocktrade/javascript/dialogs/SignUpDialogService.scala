package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalOptions}
import com.github.ldaniels528.scalascript.{Service, injected}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.models.UserProfile
import com.shocktrade.javascript.social.FacebookProfile

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
 * Sign-Up Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class SignUpDialogService($http: Http, $modal: Modal, @injected("MySession") mySession: MySession)
  extends Service {

  /**
   * Sign-up Modal Dialog
   */
  def popup(facebookID: String, fbProfile: FacebookProfile)(implicit ec: ExecutionContext) = {
    val modalInstance = $modal.open[UserProfile](ModalOptions(
      templateUrl = "sign_up_dialog.htm",
      controller = classOf[SignUpDialogController].getSimpleName
    ))
    modalInstance.result
  }

  def createAccount(form: js.Dynamic)(implicit ec: ExecutionContext) = $http.post[js.Dynamic]("/api/profile/create", form)

}
