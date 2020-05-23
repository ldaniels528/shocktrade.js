package com.shocktrade.client.dialogs

import com.shocktrade.client.contest.ContestService
import com.shocktrade.client.dialogs.NewGameDialogController._
import com.shocktrade.common.forms.ContestCreationForm._
import com.shocktrade.common.forms.{ContestCreationForm, ContestCreationResponse}
import com.shocktrade.common.models.user.UserProfile
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.npm.angularjs.{Service, Timeout, _}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * New Game Dialog Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewGameDialog($uibModal: Modal) extends Service {

  /**
   * Sign-up Modal Dialog
   */
  def popup(userID: String): js.Promise[NewGameDialogResult] = {
    val $uibModalInstance = $uibModal.open[NewGameDialogResult](new ModalOptions(
      templateUrl = "new_game_dialog.html",
      controller = classOf[NewGameDialogController].getSimpleName,
      resolve = js.Dictionary("userID" -> (() => userID))
    ))
    $uibModalInstance.result
  }

}

/**
 * New Game Dialog Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewGameDialogController($scope: NewGameDialogControllerScope, $timeout: Timeout, toaster: Toaster,
                              $uibModalInstance: ModalInstance[NewGameDialogResult],
                              @injected("ContestService") contestService: ContestService,
                              @injected("NewGameDialog") newGameDialog: NewGameDialog,
                              @injected("userID") userID: => String)
  extends Controller {

  private val errors = emptyArray[String]
  private var processing = false

  /////////////////////////////////////////////////////////////////////////////
  //			Public Data Structures
  /////////////////////////////////////////////////////////////////////////////

  $scope.durations = GameDurations
  $scope.levelCaps = LevelCaps
  $scope.startingBalances = StartingBalances
  $scope.form = new ContestCreationForm(
    perksAllowed = true,
    robotsAllowed = true,
    startAutomatically = true,
    startingBalance = StartingBalances.headOption.orUndefined,
    duration = GameDurations.headOption.orUndefined
  )

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.cancel = () => $uibModalInstance.dismiss("cancel")

  $scope.createGame = (aForm: js.UndefOr[ContestCreationForm]) => aForm foreach { form =>
    if (isValidForm(form)) {
      processing = true
      val promise = $timeout(() => processing = false, 30.seconds)

      // add the player info
      $scope.form.userID = userID

      // create the new game
      contestService.createContest(form.toRequest).toFuture onComplete {
        case Success(response) =>
          $uibModalInstance.close(response.data)
          $timeout.cancel(promise)
          processing = false
        case Failure(e) =>
          console.error(s"Error creating New Game: ${e.getMessage} => form = ${angular.toJson(form)}")
          $timeout.cancel(promise)
          processing = false
      }
    }
  }

  $scope.enforceInvitationOnly = () => $scope.form.friendsOnly = false

  $scope.isProcessing = () => processing

  $scope.getMessages = () => errors

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def isValidForm(form: ContestCreationForm) = {
    errors.removeAll()
    if (!isDefined(form.name) || form.name.exists(_.isEmpty)) errors.push("Game Title is required")
    if (!isDefined(form.duration)) errors.push("Game Duration is required")
    errors.isEmpty
  }

}

/**
 * New Game Dialog Controller Singleton
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object NewGameDialogController {
  type NewGameDialogResult = ContestCreationResponse

  /**
   * New Game Dialog Controller Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait NewGameDialogControllerScope extends Scope {
    // variables
    var durations: js.Array[GameDuration] = js.native
    var form: ContestCreationForm = js.native
    var levelCaps: js.Array[LevelCap] = js.native
    var startingBalances: js.Array[GameBalance] = js.native
    var userProfile: js.UndefOr[UserProfile] = js.native

    // functions
    var cancel: js.Function0[Unit] = js.native
    var createGame: js.Function1[js.UndefOr[ContestCreationForm], Unit] = js.native
    var enforceInvitationOnly: js.Function0[Unit] = js.native
    var getMessages: js.Function0[js.Array[String]] = js.native
    var isProcessing: js.Function0[Boolean] = js.native

  }

}

