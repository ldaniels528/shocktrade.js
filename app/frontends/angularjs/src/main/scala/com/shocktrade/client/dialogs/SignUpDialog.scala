package com.shocktrade.client.dialogs

import com.shocktrade.client.dialogs.SignUpDialogController.SignUpDialogResult
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.users.UserService
import com.shocktrade.common.forms.SignUpForm
import com.shocktrade.common.util.StringHelper._
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.http.Http
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.npm.angularjs.{Timeout, _}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Sign-Up Dialog Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class SignUpDialog($http: Http, $uibModal: Modal) extends Service {

  def signUp(): js.Promise[SignUpDialogResult] = {
    val modalInstance = $uibModal.open[SignUpDialogResult](new ModalOptions(
      controller = classOf[SignUpDialogController].getSimpleName,
      templateUrl = "sign_up_dialog.html"
    ))
    modalInstance.result
  }

}

/**
 * Sign-Up Dialog Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class SignUpDialogController($scope: SignUpDialogScope, $uibModalInstance: ModalInstance[SignUpDialogResult],
                                  $timeout: Timeout, toaster: Toaster,
                                  @injected("UserService") accountService: UserService)
  extends Controller {

  private val messages = emptyArray[String]
  private var loading = false

  $scope.form = new SignUpForm()

  ///////////////////////////////////////////////////////////////////////
  //    Public Functions
  ///////////////////////////////////////////////////////////////////////

  $scope.cancel = () => $uibModalInstance.dismiss("cancel")

  $scope.createAccount = (aForm: js.UndefOr[SignUpForm]) => aForm foreach { form =>
    if (isValid(form)) {
      startLoading()
      val outcome = accountService.createAccount(form)
      outcome onComplete {
        case Success(response) if response.status != 200 =>
          messages.push(response.statusText)
        case Success(response) =>
          val profile = response.data
          $uibModalInstance.close(profile)
        case Failure(e) =>
          toaster.error(e.getMessage)
          console.log(s"Error registering user: ${e.getMessage}")
      }
      outcome onComplete { _ => stopLoading() }
    }
  }

  $scope.isLoading = () => loading

  $scope.getMessages = () => messages

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
    if (!isDefined(form.username) || form.username.exists(_.isBlank)) {
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

  private def startLoading(): Unit = loading = true

  private def stopLoading(): js.Promise[js.Any] = $timeout(() => loading = false, 1.second)

}

/**
 * Sign-Up Dialog Controller Singleton
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object SignUpDialogController {

  type SignUpDialogResult = UserProfile

}

/**
 * Sign-Up Dialog Scope
 */
@js.native
trait SignUpDialogScope extends js.Object {
  // variables
  var form: SignUpForm

  // functions
  var cancel: js.Function0[Unit]
  var createAccount: js.Function1[js.UndefOr[SignUpForm], Unit]
  var getMessages: js.Function0[js.Array[String]]
  var isLoading: js.Function0[Boolean]

}


