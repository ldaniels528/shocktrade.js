package com.shocktrade.client.dialogs

import com.shocktrade.client.contest.ContestService
import com.shocktrade.client.dialogs.NewGameDialogController._
import com.shocktrade.client.models.UserProfile
import com.shocktrade.common.forms.ContestCreationForm.{GameBalance, GameDuration, LevelCap}
import com.shocktrade.common.forms.{ContestCreationForm, ContestCreationResponse}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.npm.angularjs.{Service, Timeout, _}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.duration._
import scala.language.postfixOps
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
  def popup(): js.Promise[NewGameDialogResult] = {
    val $uibModalInstance = $uibModal.open[NewGameDialogResult](new ModalOptions(
      templateUrl = "new_game_dialog.html",
      controller = classOf[NewGameDialogController].getSimpleName
    ))
    $uibModalInstance.result
  }

}

/**
 * New Game Dialog Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewGameDialogController($scope: NewGameDialogScope, $timeout: Timeout, toaster: Toaster,
                              $uibModalInstance: ModalInstance[NewGameDialogResult],
                              @injected("ContestService") contestService: ContestService,
                              @injected("NewGameDialog") newGameDialog: NewGameDialog)
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
    startingBalance = StartingBalances.headOption.orUndefined
  )

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.cancel = () => $uibModalInstance.dismiss("cancel")

  $scope.createGame = (aForm: js.UndefOr[ContestCreationForm]) => aForm foreach { form =>
    if (isValidForm(form)) {
      $scope.userProfile.flatMap(_.userID).toOption match {
        case Some(userId) =>
          processing = true
          val promise = $timeout(() => processing = false, 30.seconds)

          // add the player info
          $scope.form.userID = userId

          // create the new game
          contestService.createNewGame(form).toFuture onComplete {
            case Success(response) =>
              $uibModalInstance.close(response.data)
              $timeout.cancel(promise)
              processing = false
            case Failure(e) =>
              console.error(s"Error creating New Game: ${e.getMessage} => form = ${angular.toJson(form)}")
              $timeout.cancel(promise)
              processing = false
          }
        case None =>
          toaster.error("User is not authenticated")
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

    if ($scope.userProfile.flatMap(_.userID).isEmpty) errors.push("You must login to create games")
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

  val GameDurations: js.Array[GameDuration] = js.Array(
    new GameDuration(label = "1 Week", value = 7),
    new GameDuration(label = "2 Weeks", value = 14),
    new GameDuration(label = "3 Weeks", value = 21),
    new GameDuration(label = "4 Weeks", value = 28),
    new GameDuration(label = "5 Weeks", value = 35),
    new GameDuration(label = "6 Weeks", value = 42))

  val LevelCaps: js.Array[LevelCap] = (1 to 25) map { n => new LevelCap(label = s"Level $n", value = n) } toJSArray

  val StartingBalances: js.Array[GameBalance] = js.Array(
    new GameBalance(label = "$ 1,000", value = 1000.00),
    new GameBalance(label = "$ 2,500", value = 2500.00),
    new GameBalance(label = "$ 5,000", value = 5000.00),
    new GameBalance(label = "$10,000", value = 10000.00),
    new GameBalance(label = "$25,000", value = 25000.00),
    new GameBalance(label = "$50,000", value = 50000.00),
    new GameBalance(label = "$75,000", value = 75000.00),
    new GameBalance(label = "$100,000", value = 100000.00))

}

/**
 * New Game Dialog Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait NewGameDialogScope extends Scope {
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

