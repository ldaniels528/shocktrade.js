package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.meansjs.angularjs.Timeout
import com.github.ldaniels528.meansjs.angularjs.uibootstrap.{ModalInstance, ModalOptions}
import com.github.ldaniels528.meansjs.angularjs.http.Http
import com.github.ldaniels528.meansjs.angularjs.toaster.Toaster
import com.github.ldaniels528.meansjs.angularjs.uibootstrap.Modal
import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.github.ldaniels528.meansjs.angularjs.{Service, _}
import com.shocktrade.javascript.MySessionService
import com.shocktrade.javascript.dialogs.NewGameDialogController.{NewGameDialogResult, _}
import com.shocktrade.javascript.models.{Contest, PlayerInfo}
import org.scalajs.dom.console

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
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
    $http.put[Contest]("/api/contest", form)
  }
}

/**
  * New Game Dialog Controller
  * @author lawrence.daniels@gmail.com
  */
class NewGameDialogController($scope: NewGameDialogScope, $http: Http,
                              $modalInstance: ModalInstance[NewGameDialogResult], $timeout: Timeout, toaster: Toaster,
                              @injected("MySessionService") mySession: MySessionService,
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

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  $scope.createGame = (aForm: js.UndefOr[NewGameForm]) =>  aForm foreach { form =>
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
  * New Game Dialog Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewGameDialogScope extends Scope {
  // variables
  var durations: js.Array[GameDuration]
  var form: NewGameForm
  var restrictionTypes: js.Array[js.Dynamic]
  var startingBalances: js.Array[Int]

  // functions
  var cancel: js.Function0[Unit]
  var createGame: js.Function1[js.UndefOr[NewGameForm], Unit]
  var enforceInvitationOnly: js.Function0[Unit]
  var getMessages : js.Function0[js.Array[String]]
  var isProcessing: js.Function0[Boolean]

}

/**
  * Game Duration
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GameDuration extends js.Object {
  var label: String
  var value: Int
}

/**
  * Game Duration Singleton
  * @author lawrence.daniels@gmail.com
  */
object GameDuration {

  def apply(label: String, value: Int) = {
    val duration = New[GameDuration]
    duration.label = label
    duration.value = value
    duration
  }
}

/**
  * New Game Dialog Form
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewGameForm extends js.Object {
  var name: js.UndefOr[String]
  var duration: js.UndefOr[GameDuration]
  var friendsOnly: js.UndefOr[Boolean]
  var perksAllowed: js.UndefOr[Boolean]
  var player: PlayerInfo
  var robotsAllowed: js.UndefOr[Boolean]
  var startAutomatically: js.UndefOr[Boolean]
  var startingBalance: js.UndefOr[Int]
}

/**
  * New Game Dialog Form Singleton
  * @author lawrence.daniels@gmail.com
  */
object NewGameForm {

  def apply(duration: js.UndefOr[GameDuration] = js.undefined,
            perksAllowed: js.UndefOr[Boolean] = js.undefined,
            robotsAllowed: js.UndefOr[Boolean] = js.undefined,
            startAutomatically: js.UndefOr[Boolean] = js.undefined,
            startingBalance: js.UndefOr[Int] = js.undefined) = {
    val form = New[NewGameForm]
    form.duration = duration
    form.perksAllowed = perksAllowed
    form.robotsAllowed = robotsAllowed
    form.startAutomatically = startAutomatically
    form.startingBalance = startingBalance
    form
  }
}
