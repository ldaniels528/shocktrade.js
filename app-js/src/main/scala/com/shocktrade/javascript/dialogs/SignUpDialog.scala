package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript._
import com.github.ldaniels528.scalascript.core.TimerConversions._
import com.github.ldaniels528.scalascript.core.{Http, Timeout}
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalInstance, ModalOptions, Toaster}
import com.github.ldaniels528.scalascript.social.facebook.{FacebookProfileResponse, FacebookService}
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.SignUpDialogController.SignUpDialogResult
import com.shocktrade.javascript.models.UserProfile
import org.scalajs.dom.console

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Sign-Up Dialog Service
  * @author lawrence.daniels@gmail.com
  */
class SignUpDialog($http: Http, $modal: Modal) extends Service {

  def popup()(implicit ec: ExecutionContext) = {
    val modalInstance = $modal.open[SignUpDialogResult](ModalOptions(
      templateUrl = "sign_up_dialog.htm",
      controllerClass = classOf[SignUpDialogController]
    ))
    modalInstance.result
  }

  def createAccount(form: SignUpForm)(implicit ec: ExecutionContext) = $http.post[UserProfile]("/api/profile/create", form)

}

/**
  * Sign-Up Dialog Controller
  * @author lawrence.daniels@gmail.com
  */
class SignUpDialogController($scope: SignUpDialogScope, $modalInstance: ModalInstance[SignUpDialogResult],
                             $timeout: Timeout, toaster: Toaster,
                             @injected("Facebook") facebook: FacebookService,
                             @injected("SignUpDialog") dialog: SignUpDialog)
  extends Controller {

  private val messages = emptyArray[String]
  private var loading = false

  $scope.form = SignUpForm()

  ///////////////////////////////////////////////////////////////////////
  //    Public Functions
  ///////////////////////////////////////////////////////////////////////

  @scoped def cancel() = $modalInstance.dismiss("cancel")

  @scoped def createAccount(form: SignUpForm) = registerUser(form)

  @scoped def getMessages = messages

  @scoped def isLoading = loading

  @scoped def loadFacebookProfile() {
    facebook.getUserProfile onComplete {
      case Success(fbProfile) =>
        console.log(s"facebook.profile = ${angular.toJson(fbProfile)}")
        $scope.form.fbProfile = fbProfile
        $scope.form.name = fbProfile.name
        $scope.form.facebookID = fbProfile.id
      case Failure(e) =>
        toaster.error("Error loading Facebook profile")
        console.error(s"Error loading Facebook profile: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  ///////////////////////////////////////////////////////////////////////
  //    Private Functions
  ///////////////////////////////////////////////////////////////////////

  /**
    * Validates the form
    * @param form the given [[SignUpForm form]]
    * @return {boolean}
    */
  private def isValid(form: SignUpForm) = {
    // clear messages
    messages.removeAll()

    // validate the user name
    if (!isDefined(form.userName) || form.userName.exists(_.isBlank)) {
      messages.push("Screen Name is required")
    }

    // validate the email address
    if (!isDefined(form.email) || form.email.exists(_.isBlank)) {
      messages.push("Email Address is required")
    }

    if (isDefined(form.email) && form.email.exists(!_.isValidEmail)) {
      messages.push("The Email Address format is invalid")
    }

    // it"s valid is the messages are empty
    messages.isEmpty
  }

  private def registerUser(form: SignUpForm) {
    if (isValid(form)) {
      startLoading()
      dialog.createAccount(form) onComplete {
        case Success(profile) =>
          stopLoading()
          if (!isDefined(profile.dynamic.error)) $modalInstance.close((profile, form.fbProfile))
          else {
            messages.push(profile.dynamic.error.as[String])
          }

        case Failure(e) =>
          stopLoading()
          toaster.error(e.getMessage)
          console.log(s"Error registering user: ${e.getMessage}")
      }
    }
  }

  private def startLoading() = loading = true

  private def stopLoading() = $timeout(() => loading = false, 1.second)

}

/**
  * Sign-Up Dialog Controller Singleton
  */
object SignUpDialogController {

  type SignUpDialogResult = (UserProfile, FacebookProfileResponse)
}

/**
  * Sign-Up Dialog Scope
  */
trait SignUpDialogScope extends js.Object {
  var form: SignUpForm = js.native
}

/**
  * Sign-Up Dialog Form
  */
trait SignUpForm extends js.Object {
  var name: js.UndefOr[String] = js.native
  var facebookID: js.UndefOr[String] = js.native
  var userName: js.UndefOr[String] = js.native
  var email: js.UndefOr[String] = js.native
  var fbProfile: FacebookProfileResponse = js.native
}

/**
  * Sign-Up Dialog Form Singleton
  */
object SignUpForm {

  def apply() = makeNew[SignUpForm]
}

