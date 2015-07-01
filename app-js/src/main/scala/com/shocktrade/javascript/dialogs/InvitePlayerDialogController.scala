package com.shocktrade.javascript.dialogs

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.{ScalaJsHelper, Controller}
import com.ldaniels528.javascript.angularjs.core.ModalInstance
import com.shocktrade.javascript.MySession
import ScalaJsHelper._

import scala.scalajs.js

/**
 * Invite Player Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class InvitePlayerDialogController($scope: js.Dynamic, $modalInstance: ModalInstance[js.Array[js.Dynamic]],
                                   @named("MySession") mySession: MySession,
                                   @named("myFriends") myFriends: js.Array[js.Dynamic])
  extends Controller {

  private val invites = emptyArray[js.Dynamic]

  $scope.getFriends = () => mySession.fbFriends

  $scope.getInvitedCount = () => invites.count(invitee => isDefined(invitee))

  $scope.getInvites = () => invites

  $scope.ok = () => $modalInstance.close(getSelectedFriends)

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  private def getSelectedFriends = {
    val selectedFriends = emptyArray[js.Dynamic]
    for (n <- 0 to invites.length) {
      if (isDefined(invites(n))) selectedFriends.push(myFriends(n))
    }
    selectedFriends
  }

}
