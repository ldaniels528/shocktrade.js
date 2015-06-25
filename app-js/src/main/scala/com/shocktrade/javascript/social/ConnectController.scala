package com.shocktrade.javascript.social

import biz.enef.angulate.{Scope, ScopeController, named}
import com.ldaniels528.javascript.angularjs.extensions.Toaster
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.ComposeMessageDialogService

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Connect Controller
 * @author lawrence.daniels@gmail.com
 */
class ConnectController($scope: js.Dynamic, toaster: Toaster,
                        @named("ComposeMessageDialog") messageDialog: ComposeMessageDialogService,
                        @named("ConnectService") connectService: ConnectService,
                        @named("MySession") mySession: MySession)
  extends ScopeController {

  private val scope = $scope.asInstanceOf[Scope]
  private var myUpdates = emptyArray[js.Dynamic]
  private var myUpdate: js.Dynamic = null
  private var contact: js.Dynamic = JS()

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.chooseContact = (friend: js.Dynamic) => chooseContact(friend)

  $scope.chooseFirstContact = () => mySession.fbFriends.headOption foreach ($scope chooseContact _)

  $scope.composeMessage = () => composeMessage()

  /**
   * Composes a new message via pop-up dialog
   */
  private def composeMessage() = {
    messageDialog.popup() onComplete {
      case Success(response) => loadMyUpdates(mySession.getUserName())
      case Failure(e) =>
        toaster.error(e.getMessage)
    }
  }

  $scope.getContact = () => contact

  $scope.getFriends = () => mySession.fbFriends

  $scope.deleteMessages = (userName: js.UndefOr[String]) => userName foreach deleteMessages

  $scope.getMyUpdates = () => myUpdates

  $scope.getSelectedUpdate = () => myUpdate

  /**
   * Selects a specific message
   */
  $scope.selectUpdate = (entry: js.Dynamic) => myUpdate = entry

  $scope.getUserInfo = (fbUserId: String) => getUserInfo(fbUserId)

  $scope.identifyFacebookFriends = (fbFriends: js.Array[js.Dynamic]) => identifyFacebookFriends(fbFriends)

  $scope.getContactList = (searchTerm: js.UndefOr[String]) => getContactList(searchTerm)

  $scope.loadMyUpdates = (userName: String) => loadMyUpdates(userName)

  /**
   * Selects all currently visible messages
   */
  $scope.selectAll = (checked: Boolean) => myUpdates.foreach(_.selected = checked)

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Selects a specific contact/friend
   */
  private def chooseContact(friend: js.Dynamic) = {
    //g.console.log(s"contact = ${JSON.stringify(friend, null, "\t")}")
    contact = friend
    $scope.getUserInfo(friend.id)
  }

  /**
   * Retrieve a limited set of user profile information for a specific contact/friend
   */
  private def getUserInfo(fbUserId: String) = {
    withLoading($scope)(connectService.getUserInfo(fbUserId)) onComplete {
      case Success(profile) =>
        contact.profile = profile
      case Failure(e) =>
        g.console.log(s"Failed to retrieve profile for contact ${contact.name}")
        toaster.error(s"Failed to retrieve the user profile for contact ${contact.name}")
    }
  }

  private def identifyFacebookFriends(fbFriends: js.Array[js.Dynamic]) = {
    $scope.startLoading()
    connectService.identifyFacebookFriends(fbFriends) onComplete {
      case Success(_) => $scope.stopLoading()
      case Failure(e) => $scope.stopLoading()
    }
  }

  /**
   * Returns the contacts matching the given search term
   */
  private def getContactList = (searchTerm: js.UndefOr[String]) => {
    // TODO reinstate search
    /*
    g.console.log(s"searchTerm = $searchTerm (${Option(searchTerm).map(_.getClass.getName).orNull})")
    searchTerm foreach { mySearchTerm =>
      val fbFriends = mySession.fbFriends
      if (!Option(searchTerm).exists(_.nonEmpty)) fbFriends.take(40)
      else {
        val term = mySearchTerm.trim.toLowerCase
        fbFriends.filter(friend => isDefined(friend.name) && friend.name.as[String].toLowerCase.contains(term))
      }
    }*/
    mySession.fbFriends
  }

  /**
   * Loads updates from the database
   */
  private def loadMyUpdates(userName: String) = {
    if (userName.nonBlank) {
      $scope.startLoading()

      connectService.getUserUpdates(userName, 50) onComplete {
        case Success(data) =>
          $scope.stopLoading()
          myUpdates = data
          myUpdate = null
          data.foreach(_.selected = false)
          $scope.loading = false

        case Failure(e) =>
          $scope.stopLoading()
          g.console.log("Failed to load Connect")
          e.printStackTrace()
          toaster.pop("error ", "Failed to load Connect", null)
          $scope.loading = false
      }
    }
  }

  /**
   * Deletes selected messages
   */
  private def deleteMessages(userName: String) {
    $scope.startLoading()

    // gather the records to delete
    val messageIDs = myUpdates.filter(update => isDefined(update.selected) && update.selected.isTrue).map(_.OID)

    // delete the records
    g.console.log(s"messageIDs = ${JSON.stringify(messageIDs)}")
    if (messageIDs.nonEmpty) {
      connectService.deleteMessages(messageIDs) onComplete {
        case Success(response) =>
          $scope.stopLoading()
          loadMyUpdates(userName)

        case Failure(e) =>
          $scope.stopLoading()
          toaster.error("Failed to delete message ")
      }
    }
    else {
      $scope.stopLoading()
      toaster.error("No message(s) selected")
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Event Listeners
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Listen for changes to the player's profile
   */
  scope.$on(UserProfileChanged, { (profile: js.Dynamic) =>
    if (mySession.getRecentSymbols().nonEmpty) {
      loadMyUpdates(mySession.getUserName())
      //$scope.chooseFirstContact()
    }
  })

}
