package com.shocktrade.client.dialogs

import com.shocktrade.client.MySessionService
import com.shocktrade.client.dialogs.InvitePlayerDialogController.InvitePlayerDialogResult
import com.shocktrade.common.models.contest.Participant
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.http.Http
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.social.facebook.TaggableFriend
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.scalajs.js

/**
  * Invite Player Dialog Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class InvitePlayerDialog($http: Http, $uibModal: Modal) extends Service {

  /**
    * Invite a player via pop-up dialog
    */
  def popup(participant: Participant): Future[InvitePlayerDialogResult] = {
    val modalInstance = $uibModal.open[InvitePlayerDialogResult](new ModalOptions(
      templateUrl = "invite_player_dialog.html",
      controller = classOf[InvitePlayerDialogController].getSimpleName
    ))
    modalInstance.result
  }
}

/**
  * Invite Player Dialog Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class InvitePlayerDialogController($scope: InvitePlayerScope, $uibModalInstance: ModalInstance[InvitePlayerDialogResult],
                                   @injected("MySessionService") mySession: MySessionService)
  extends Controller {

  private val myFriends = emptyArray[TaggableFriend]
  $scope.invites = emptyArray[TaggableFriend]

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.getFriends = () => myFriends

  $scope.getInvitedCount = () => $scope.invites.count(invitee => isDefined(invitee))

  $scope.getInvites = () => $scope.invites

  $scope.ok = () => $uibModalInstance.close(getSelectedFriends)

  $scope.cancel = () => $uibModalInstance.dismiss("cancel")

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
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object InvitePlayerDialogController {
  type InvitePlayerDialogResult = js.Array[TaggableFriend]

}

/**
  * Invite Player Dialog Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
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