package com.shocktrade.client.users

import com.shocktrade.client.dialogs.SignUpDialogController.SignUpDialogResult
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.users.SignInDialogController.SignInDialogResult
import com.shocktrade.common.auth.AuthenticationForm
import com.shocktrade.common.util.StringHelper._
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.http.Http
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.npm.angularjs.{Timeout, _}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper.{emptyArray, isDefined, _}

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
                             @injected("AuthenticationService") authenticationService: AuthenticationService)
  extends Controller {

  private val messages:js.Array[String] = emptyArray
  private var loading:Boolean = false

  $scope.form = new AuthenticationForm()

  ///////////////////////////////////////////////////////////////////////
  //    Public Functions
  ///////////////////////////////////////////////////////////////////////

  $scope.cancel = () => $uibModalInstance.dismiss("cancel")

  $scope.signIn = (aForm: js.UndefOr[AuthenticationForm]) => aForm foreach { form =>
    if (isValid(form)) {
      startLoading()

      // attempt to authenticate the user
      val outcome = for {
        code <- authenticationService.getCode
        response <- authenticationService.login(form.copy(authCode = code.data.code))
      } yield response

      outcome onComplete {
        case Success(response) =>
          val profile = response.data
          stopLoading()
          if (!isDefined(profile.dynamic.error)) $uibModalInstance.close(profile)
          else {
            profile.dynamic.error.asOpt[String] foreach (messages.push(_))
          }

        case Failure(e) =>
          stopLoading()
          toaster.error(e.getMessage)
          console.log(s"Error authenticating user: ${e.getMessage}")
      }
    }
  }

  $scope.isLoading = () => loading

  $scope.getMessages = () => messages

  ///////////////////////////////////////////////////////////////////////
  //    Private Functions
  ///////////////////////////////////////////////////////////////////////

  /**
   * Validates the form
   * @param form the given [[AuthenticationForm form]]
   * @return {boolean}
   */
  private def isValid(form: AuthenticationForm): Boolean = {
    // clear messages
    messages.removeAll()

    // validate the user name
    if (!isDefined(form.username) || form.username.exists(_.isBlank)) {
      messages.push("Screen Name is required")
    }

    // validate the email address
    if (!isDefined(form.password) || form.password.exists(_.isBlank)) {
      messages.push("Password is required")
    }

    // it"s valid is the messages are empty
    messages.isEmpty
  }

  private def startLoading(): Unit = loading = true

  private def stopLoading(): js.Promise[js.Any] = $timeout(() => loading = false, 1.second)

}

/**
 * Sign-Up Dialog Controller Singleton
 */
object SignInDialogController {

  type SignInDialogResult = UserProfile

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
