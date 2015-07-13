package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.extensions.ModalInstance
import com.github.ldaniels528.scalascript.{Controller, Scope, injected, scoped}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.InvitePlayerDialogController.InvitePlayerDialogResult
import com.shocktrade.javascript.social.TaggableFriend

import scala.scalajs.js

/**
 * Invite Player Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class InvitePlayerDialogController($scope: InvitePlayerScope, $modalInstance: ModalInstance[InvitePlayerDialogResult],
                                   @injected("MySession") mySession: MySession,
                                   @injected("myFriends") myFriends: js.Array[TaggableFriend])
  extends Controller {

  private val invites = emptyArray[TaggableFriend]

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  @scoped def getFriends = mySession.fbFriends

  @scoped def getInvitedCount = invites.count(invitee => isDefined(invitee))

  @scoped def getInvites = invites

  @scoped def ok() = $modalInstance.close(getSelectedFriends)

  @scoped def cancel() = $modalInstance.dismiss("cancel")

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def getSelectedFriends = {
    val selectedFriends = emptyArray[TaggableFriend]
    for (n <- 0 to invites.length) {
      if (isDefined(invites(n))) selectedFriends.push(myFriends(n))
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

}