package com.shocktrade.client.users

import com.shocktrade.client.dialogs.SignUpDialogController.SignUpDialogResult
import com.shocktrade.client.users.SignInDialogController.{AuthenticationFormValidation, SignInDialogResult}
import com.shocktrade.common.auth.AuthenticationForm
import com.shocktrade.common.models.user.UserProfile
import com.shocktrade.common.util.StringHelper._
import io.scalajs.npm.angularjs.http.Http
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.npm.angularjs.{Timeout, _}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper.{emptyArray, isDefined}

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Sign-In Dialog Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class SignInDialog($http: Http, $uibModal: Modal) extends Service {

  def signIn(): js.Promise[SignUpDialogResult] = {
    val modalInstance = $uibModal.open[SignInDialogResult](new ModalOptions(
      templateUrl = "sign_in_dialog.html",
      controller = classOf[SignInDialogController].getSimpleName
    ))
    modalInstance.result
  }

}

/**
 * Sign-Up Dialog Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class SignInDialogController($scope: SignInDialogScope, $uibModalInstance: ModalInstance[SignInDialogResult],
                                  $timeout: Timeout, toaster: Toaster,
                                  @injected("UserService") userService: UserService)
  extends Controller {

  private val messages: js.Array[String] = emptyArray
  private var loading: Boolean = false

  $scope.form = new AuthenticationForm()

  ///////////////////////////////////////////////////////////////////////
  //    Public Functions
  ///////////////////////////////////////////////////////////////////////

  $scope.cancel = () => $uibModalInstance.dismiss("cancel")

  $scope.signIn = (aForm: js.UndefOr[AuthenticationForm]) => aForm foreach { form =>
    if (form.isValid(messages)) {
      startLoading()

      // attempt to authenticate the user
      val outcome = for {
        authCode <- userService.getCode
        userAccount <- userService.login(form.copy(authCode = authCode.data.code))
      } yield userAccount

      outcome onComplete {
        case Success(userProfile) =>
          stopLoading()
          $uibModalInstance.close(userProfile.data)
        case Failure(e) =>
          stopLoading()
          $uibModalInstance.dismiss(e.getMessage)
      }
    }
  }

  $scope.isLoading = () => loading

  $scope.getMessages = () => messages

  ///////////////////////////////////////////////////////////////////////
  //    Private Functions
  ///////////////////////////////////////////////////////////////////////

  private def startLoading(): Unit = loading = true

  private def stopLoading(): js.Promise[js.Any] = $timeout(() => loading = false, 1.second)

}

/**
 * Sign-Up Dialog Controller Singleton
 */
object SignInDialogController {

  type SignInDialogResult = UserProfile

  /**
   * Authentication Form Validation
   * @param form the given [[AuthenticationForm form]]
   */
  final implicit class AuthenticationFormValidation(val form: AuthenticationForm) extends AnyVal {

    /**
     * Validates the form
     * @return {boolean}
     */
    def isValid(messages: js.Array[String]): Boolean = {
      // clear the old messages
      messages.clear()

      // validate the user name
      if (!isDefined(form.username) || form.username.exists(_.isBlank)) {
        messages.push("Screen Name is required")
      }

      // validate the email address
      if (!isDefined(form.password) || form.password.exists(_.isBlank)) {
        messages.push("Password is required")
      }

      messages.isEmpty
    }
  }

}

/**
 * Sign-Up Dialog Scope
 */
@js.native
trait SignInDialogScope extends js.Object {
  // variables
  var form: AuthenticationForm

  // functions
  var cancel: js.Function0[Unit]
  var getMessages: js.Function0[js.Array[String]]
  var isLoading: js.Function0[Boolean]
  var signIn: js.Function1[js.UndefOr[AuthenticationForm], Unit]

}
