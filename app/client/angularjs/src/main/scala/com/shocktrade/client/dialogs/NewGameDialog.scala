package com.shocktrade.client.dialogs

import com.shocktrade.client.MySessionService
import com.shocktrade.client.dialogs.NewGameDialogController.{NewGameDialogResult, _}
import com.shocktrade.common.forms.NewGameForm
import com.shocktrade.common.forms.NewGameForm.GameDuration
import com.shocktrade.common.models.PlayerRef
import com.shocktrade.client.models.contest.Contest
import org.scalajs.angularjs.http.Http
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import org.scalajs.angularjs.{Service, Timeout, _}
import org.scalajs.dom.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * New Game Dialog Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NewGameDialog($http: Http, $modal: Modal) extends Service {

  /**
    * Sign-up Modal Dialog
    */
  def popup(): Future[NewGameDialogResult] = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[NewGameDialogResult](new ModalOptions(
      templateUrl = "new_game_dialog.html",
      controller = classOf[NewGameDialogController].getSimpleName
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
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
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

  $scope.durations = GameDurations
  $scope.startingBalances = StartingBalances
  $scope.restrictionTypes = emptyArray[js.Dynamic]

  $scope.form = new NewGameForm(
    perksAllowed = true,
    robotsAllowed = true,
    startAutomatically = true,
    startingBalance = StartingBalances.head
  )

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  $scope.createGame = (aForm: js.UndefOr[NewGameForm]) => aForm foreach { form =>
    if (isValidForm(form)) {
      mySession.userProfile._id.toOption match {
        case Some(userId) =>
          processing = true
          val promise = $timeout(() => processing = false, 30.seconds)

          // add the player info
          $scope.form.player = new PlayerRef(
            _id = userId,
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
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object NewGameDialogController {

  type NewGameDialogResult = Contest

  val GameDurations = js.Array(
    new GameDuration(label = "1 Week", value = 7),
    new GameDuration(label = "2 Weeks", value = 14),
    new GameDuration(label = "1 Month", value = 30))

  val StartingBalances = js.Array(1000, 2500, 5000, 10000, 25000, 50000, 100000)

}

/**
  * New Game Dialog Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait NewGameDialogScope extends Scope {
  // variables
  var durations: js.Array[GameDuration] = js.native
  var form: NewGameForm = js.native
  var restrictionTypes: js.Array[js.Dynamic] = js.native
  var startingBalances: js.Array[Int] = js.native

  // functions
  var cancel: js.Function0[Unit] = js.native
  var createGame: js.Function1[js.UndefOr[NewGameForm], Unit] = js.native
  var enforceInvitationOnly: js.Function0[Unit] = js.native
  var getMessages: js.Function0[js.Array[String]] = js.native
  var isProcessing: js.Function0[Boolean] = js.native

}

