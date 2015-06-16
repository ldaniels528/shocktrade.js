package com.shocktrade.javascript.dialogs

import biz.enef.angulate.core.{HttpService, Timeout}
import biz.enef.angulate.{ScopeController, Service, named}
import com.greencatsoft.angularjs.core.Promise
import com.greencatsoft.angularjs.extensions.{ModalInstance, ModalOptions, ModalService}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.SignUpDialog._
import com.shocktrade.javascript.profile.FacebookService

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Sign-Up Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class SignUpDialog($http: HttpService, $modal: ModalService, @named("MySession") mySession: MySession) extends Service {

  /**
   * Sign-up Modal Dialog
   */
  def popup(facebookID: String, fbProfile: js.Dynamic): Promise = {
    val options = ModalOptions()
    options.templateUrl = "sign_up_dialog.htm"
    options.controller = classOf[SignUpController].getSimpleName

    val modalInstance = $modal.open(options)
    modalInstance.result
  }

  def createAccount(form: js.Dynamic) = {
    g.console.log(s"Creating account ${toJson(form)}")
    $http.post[js.Dynamic]("/api/profile/create", form)
  }

}

/**
 * Sign-Up Dialog Singleton
 * @author lawrence.daniels@gmail.com
 */
object SignUpDialog {

  /**
   * Sign-Up Controller
   * @author lawrence.daniels@gmail.com
   */
  class SignUpController($scope: js.Dynamic, $modalInstance: ModalInstance, $timeout: Timeout, toaster: Toaster,
                         @named("Facebook") facebook: FacebookService,
                         @named("SignUpDialog") dialog: SignUpDialog)
    extends ScopeController {

    private val messages = emptyArray[String]
    private var loading = false

    g.console.log(s"facebook.profile = ${toJson(facebook.profile)}")

    $scope.form = JS(
      name = facebook.profile.name,
      facebookID = facebook.facebookID
    )

    $scope.cancel = () => $modalInstance.dismiss("cancel")

    $scope.createAccount = (form: js.Dynamic) => registerUser(form)

    $scope.getMessages = () => messages

    $scope.isLoading = () => loading

    /**
     * Validates the form
     * @param form the given form
     * @return {boolean}
     */
    private def isValid(form: js.Dynamic) = {
      // clear messages
      messages.remove(0, messages.length)

      // validate the user name
      if (!isDefined(form.userName) || form.userName.as[String].isBlank) {
        messages.push("Screen Name is required")
      }

      // validate the email address
      if (!isDefined(form.email) || form.email.as[String].isBlank) {
        messages.push("Email Address is required")
      }

      if (isDefined(form.email) && !form.email.as[String].isValidEmail) {
        messages.push("The Email Address format is invalid")
      }

      // it"s valid is the messages are empty
      messages.isEmpty
    }

    private def registerUser(form: js.Dynamic) {
      if (isValid(form)) {
        startLoading()
        dialog.createAccount(form) onComplete {
          case Success(profile) =>
            stopLoading()
            if (!isDefined(profile.error)) $modalInstance.close(profile)
            else {
              messages.push(profile.error.as[String])
            }

          case Failure(e) =>
            stopLoading()
            toaster.error(e.getMessage)
            g.console.log(s"Error registering user: ${e.getMessage}")
        }
      }
    }

    private def startLoading() = loading = true

    private def stopLoading() = $timeout(() => loading = false, 1000)

  }

}