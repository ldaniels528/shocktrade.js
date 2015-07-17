package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalInstance, ModalOptions}
import com.github.ldaniels528.scalascript.{Controller, Scope, Service, injected, scoped}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.InvitePlayerDialogController.InvitePlayerDialogResult
import com.shocktrade.javascript.models.Participant
import com.shocktrade.javascript.social.TaggableFriend

import scala.concurrent.Future
import scala.scalajs.js

/**
 * Invite Player Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class InvitePlayerDialog($http: Http, $modal: Modal) extends Service {

  /**
   * Invite a player via pop-up dialog
   */
  def popup(participant: Participant): Future[InvitePlayerDialogResult] = {
    val modalInstance = $modal.open[InvitePlayerDialogResult](ModalOptions(
      templateUrl = "invite_player_dialog.htm",
      controllerClass = classOf[InvitePlayerDialogController]
    ))
    modalInstance.result
  }
}

/**
 * Invite Player Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class InvitePlayerDialogController($scope: InvitePlayerScope, $modalInstance: ModalInstance[InvitePlayerDialogResult],
                                   @injected("MySession") mySession: MySession)
  extends Controller {

  private val myFriends = mySession.fbFriends
  $scope.invites = emptyArray[TaggableFriend]

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  @scoped def getFriends = myFriends

  @scoped def getInvitedCount = $scope.invites.count(invitee => isDefined(invitee))

  @scoped def getInvites = $scope.invites

  @scoped def ok() = $modalInstance.close(getSelectedFriends)

  @scoped def cancel() = $modalInstance.dismiss("cancel")

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def getSelectedFriends = {
    val selectedFriends = emptyArray[TaggableFriend]
    for (n <- 0 to $scope.invites.length) {
      if (isDefined($scope.invites(n))) selectedFriends.push(myFriends(n))
    }
    selectedFriends
  }

}

/**
 * Invite Player Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
object InvitePlayerDialogController {
  type InvitePlayerDialogResult = js.Array[TaggableFriend]

}

/**
 * Invite Player Dialog Scope
 * @author lawrence.daniels@gmail.com
 */
trait InvitePlayerScope extends Scope {
  var invites: js.Array[TaggableFriend] = js.native

}