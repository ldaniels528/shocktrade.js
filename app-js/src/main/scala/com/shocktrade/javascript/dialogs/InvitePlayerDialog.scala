package com.shocktrade.javascript.dialogs

import com.shocktrade.javascript.MySessionService
import com.shocktrade.javascript.dialogs.InvitePlayerDialogController.InvitePlayerDialogResult
import com.shocktrade.javascript.models.Participant
import org.scalajs.angularjs._
import org.scalajs.angularjs.http.Http
import org.scalajs.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import org.scalajs.nodejs.social.facebook.TaggableFriend
import org.scalajs.nodejs.util.ScalaJsHelper._

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
    val modalInstance = $modal.open[InvitePlayerDialogResult](new ModalOptions(
      templateUrl = "invite_player_dialog.htm",
      controller = classOf[InvitePlayerDialogController].getSimpleName
    ))
    modalInstance.result
  }
}

/**
  * Invite Player Dialog Controller
  * @author lawrence.daniels@gmail.com
  */
class InvitePlayerDialogController($scope: InvitePlayerScope, $modalInstance: ModalInstance[InvitePlayerDialogResult],
                                   @injected("MySessionService") mySession: MySessionService)
  extends Controller {

  private val myFriends = mySession.fbFriends
  $scope.invites = emptyArray[TaggableFriend]

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.getFriends = () => myFriends

  $scope.getInvitedCount = () => $scope.invites.count(invitee => isDefined(invitee))

  $scope.getInvites = () => $scope.invites

  $scope.ok = () => $modalInstance.close(getSelectedFriends)

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def getSelectedFriends = {
    js.Array(
      $scope.invites.indices flatMap { n =>
        if (isDefined($scope.invites(n))) Some(myFriends(n)) else None
      }: _*)
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
@js.native
trait InvitePlayerScope extends Scope {
  // variables
  var invites: js.Array[TaggableFriend] = js.native

  // functions
  var cancel: js.Function0[Unit] = js.native
  var getFriends: js.Function0[js.Array[TaggableFriend]] = js.native
  var getInvitedCount: js.Function0[Int] = js.native
  var getInvites: js.Function0[js.Array[TaggableFriend]] = js.native
  var ok: js.Function0[Unit] = js.native

}