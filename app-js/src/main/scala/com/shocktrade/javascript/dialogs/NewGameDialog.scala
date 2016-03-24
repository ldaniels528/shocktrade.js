package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.{Service, _}
import com.github.ldaniels528.scalascript.core.{Http, Timeout}
import com.github.ldaniels528.scalascript.core.TimerConversions._
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalInstance, ModalOptions, Toaster}
import com.shocktrade.javascript.MySession
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.NewGameDialogController.{NewGameDialogResult, _}
import com.shocktrade.javascript.models.{Contest, PlayerInfo}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Failure, Success}

/**
 * New Game Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewGameDialog($http: Http, $modal: Modal) extends Service {

  /**
   * Sign-up Modal Dialog
   */
  def popup(): Future[NewGameDialogResult] = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[NewGameDialogResult](ModalOptions(
      templateUrl = "new_game_dialog.htm",
      controllerClass = classOf[NewGameDialogController]
    ))
    $modalInstance.result
  }

  /**
   * Creates a new game
   * @return the promise of the result of creating a new game
   */
  def createNewGame(form: NewGameForm): Future[Contest] = {
    required("form", form)
    $http.put[Contest]("/api/contest", form)
  }
}

/**
 * New Game Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class NewGameDialogController($scope: NewGameDialogScope, $http: Http,
                              $modalInstance: ModalInstance[NewGameDialogResult], $timeout: Timeout, toaster: Toaster,
                              @injected("MySession") mySession: MySession,
                              @injected("NewGameDialog") newGameDialog: NewGameDialog)
  extends Controller {

  private val errors = emptyArray[String]
  private var processing = false

  /////////////////////////////////////////////////////////////////////////////
  //			Public Data Structures
  /////////////////////////////////////////////////////////////////////////////

  $scope.durations = Durations
  $scope.startingBalances = StartingBalances
  $scope.restrictionTypes = emptyArray[js.Dynamic]

  $scope.form = NewGameForm(
    perksAllowed = true,
    robotsAllowed = true,
    startAutomatically = true,
    startingBalance = StartingBalances.head
  )

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  @scoped def cancel() = $modalInstance.dismiss("cancel")

  @scoped
  def createGame(form: NewGameForm) = {
    if (isValidForm(form)) {
      mySession.userProfile._id.toOption match {
        case Some(userId) =>
          processing = true
          val promise = $timeout(() => processing = false, 30.seconds)

          // add the player info
          $scope.form.player = PlayerInfo(
            id = userId,
            name = mySession.getUserName,
            facebookID = mySession.getFacebookID
          )

          // create the new game
          newGameDialog.createNewGame(form) onComplete {
            case Success(response) =>
              $modalInstance.close(response)
              $timeout.cancel(promise)
              processing = false
            case Failure(e) =>
              g.console.error(s"Error creating New Game: ${e.getMessage} => form = ${angular.toJson(form)}")
              $timeout.cancel(promise)
              processing = false
          }
        case None =>
          toaster.error("User is not authenticated")
      }
    }

  }

  @scoped def enforceInvitationOnly() = $scope.form.friendsOnly = false

  @scoped def isProcessing = processing

  @scoped def getMessages = errors

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def isValidForm(form: NewGameForm) = {
    errors.removeAll()

    if (!mySession.isAuthenticated) errors.push("You must login to create games")
    if (!isDefined(form.name) || form.name.exists(_.isEmpty)) errors.push("Game Title is required")
    if (!isDefined(form.duration)) errors.push("Game Duration is required")
    errors.isEmpty
  }

}

/**
 * New Game Dialog Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object NewGameDialogController {

  type NewGameDialogResult = Contest

  val Durations = js.Array(
    GameDuration(label = "1 Week", value = 7),
    GameDuration(label = "2 Weeks", value = 14),
    GameDuration(label = "1 Month", value = 30))

  val StartingBalances = js.Array(1000, 2500, 5000, 10000, 25000, 50000, 100000)

}

/**
 * Game Duration
 */
@js.native
trait GameDuration extends js.Object {
  var label: String = js.native
  var value: Int = js.native
}

/**
 * Game Duration Singleton
 */
object GameDuration {

  def apply(label: String, value: Int) = {
    val duration = makeNew[GameDuration]
    duration.label = label
    duration.value = value
    duration
  }
}

/**
 * New Game Dialog Scope
 */
@js.native
trait NewGameDialogScope extends Scope {
  var durations: js.Array[GameDuration] = js.native
  var form: NewGameForm = js.native
  var restrictionTypes: js.Array[js.Dynamic] = js.native
  var startingBalances: js.Array[Int] = js.native
}

/**
 * New Game Dialog Form
 */
@js.native
trait NewGameForm extends js.Object {
  var name: js.UndefOr[String] = js.native
  var duration: js.UndefOr[GameDuration] = js.native
  var friendsOnly: js.UndefOr[Boolean] = js.native
  var perksAllowed: js.UndefOr[Boolean] = js.native
  var player: PlayerInfo = js.native
  var robotsAllowed: js.UndefOr[Boolean] = js.native
  var startAutomatically: js.UndefOr[Boolean] = js.native
  var startingBalance: js.UndefOr[Int] = js.native
}

/**
 * New Game Dialog Form Singleton
 */
object NewGameForm {

  def apply(duration: js.UndefOr[GameDuration] = js.undefined,
            perksAllowed: js.UndefOr[Boolean] = js.undefined,
            robotsAllowed: js.UndefOr[Boolean] = js.undefined,
            startAutomatically: js.UndefOr[Boolean] = js.undefined,
            startingBalance: js.UndefOr[Int] = js.undefined) = {
    val form = makeNew[NewGameForm]
    form.duration = duration
    form.perksAllowed = perksAllowed
    form.robotsAllowed = robotsAllowed
    form.startAutomatically = startAutomatically
    form.startingBalance = startingBalance
    form
  }
}
