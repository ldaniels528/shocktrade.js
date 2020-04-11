package com.shocktrade.client

import com.shocktrade.client.dialogs.PlayerProfileDialog
import com.shocktrade.client.dialogs.PlayerProfileDialogController.PlayerProfileDialogResult
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Player Profile Popup Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PlayerProfilePopupSupport {
  ref: Controller =>

  def $scope: PlayerProfilePopupSupportScope

  def playerProfileDialog: PlayerProfileDialog

  def toaster: Toaster

  $scope.popupPlayerProfile = (aUserID: js.UndefOr[String]) => aUserID map { userID =>
    console.info(s"Setting up player dialog for $userID...")
    val outcome = playerProfileDialog.popup(userID)
    outcome.toFuture onComplete {
      case Success(response) =>
        console.info(s"response = ${JSON.stringify(response)}")
      case Failure(e) =>
        toaster.error("Failed to create game")
        console.error(s"Failed to create game: ${e.displayMessage}")
    }
    outcome
  }

}

/**
 * Player Profile Popup Support Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait PlayerProfilePopupSupportScope extends Scope {
  // functions
  var popupPlayerProfile: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[PlayerProfileDialogResult]]] = js.native

}