package com.shocktrade.javascript.dialogs

import com.ldaniels528.scalascript.ScalaJsHelper._
import com.ldaniels528.scalascript.core.{Http, Timeout}
import com.ldaniels528.scalascript.extensions.ModalInstance
import com.ldaniels528.scalascript.{Controller, ScalaJsHelper, Scope, named}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.dialogs.NewGameDialogController._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * New Game Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class NewGameDialogController($rootScope: Scope, $scope: js.Dynamic, $http: Http, $modalInstance: ModalInstance[js.Dynamic], $timeout: Timeout,
                              @named("ContestService") ContestService: ContestService,
                              @named("MySession") MySession: MySession,
                              @named("NewGameDialogService") newGameDialog: NewGameDialogService)
  extends Controller {

  private val errors = emptyArray[String]
  private var processing = false

  /////////////////////////////////////////////////////////////////////////////
  //			Public Data Structures
  /////////////////////////////////////////////////////////////////////////////

  $scope.durations = durations
  $scope.startingBalances = startingBalances
  $scope.restrictionTypes = emptyArray[js.Dynamic]

  $scope.form = JS(
    duration = null,
    perksAllowed = true,
    robotsAllowed = true,
    startAutomatically = true,
    startingBalance = startingBalances.head
  )

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  $scope.createGame = (form: js.Dynamic) => createGame(form)

  $scope.enforceInvitationOnly = () => $scope.form.friendsOnly = false

  $scope.isProcessing = () => processing

  $scope.getMessages = () => errors

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def createGame(form: js.Dynamic) = {
    if (isValidForm(form)) {
      processing = true
      val promise = $timeout(() => processing = false, 30.seconds)

      // add the player info
      $scope.form.player = JS(
        id = MySession.getUserID(),
        name = MySession.getUserName(),
        facebookID = MySession.getFacebookID()
      )

      // create the new game
      newGameDialog.createNewGame(form) onComplete {
        case Success(response) =>
          $modalInstance.close(response)
          $timeout.cancel(promise)
          processing = false
        case Failure(e) =>
          g.console.error(s"Error creating New Game: ${e.getMessage} => form = ${toJson(form)}")
          $timeout.cancel(promise)
          processing = false
      }
    }
  }

  private def isValidForm(form: js.Dynamic) = {
    errors.remove(0, errors.length)

    if (!MySession.isAuthenticated()) errors.push("You must login to create games")
    if (!isDefined(form.name) || form.name.as[String].isEmpty) errors.push("Game Title is required")
    if (!isDefined(form.duration)) errors.push("Game Duration is required")
    errors.isEmpty
  }

}

/**
 * New Game Dialog Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object NewGameDialogController {

  val durations = js.Array(
    JS(label = "1 Week", value = 7),
    JS(label = "2 Weeks", value = 14),
    JS(label = "1 Month", value = 30))

  val startingBalances = js.Array(1000, 2500, 5000, 10000, 25000, 50000, 100000)

}
