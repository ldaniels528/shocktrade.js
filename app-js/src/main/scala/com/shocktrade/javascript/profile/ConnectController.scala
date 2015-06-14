package com.shocktrade.javascript.profile

import biz.enef.angulate.core.Timeout
import biz.enef.angulate.{Scope, ScopeController, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Connect Controller
 * @author lawrence.daniels@gmail.com
 */
class ConnectController($scope: js.Dynamic, toaster: Toaster,
                        @named("ComposeMessageDialog") composeMessageDialog: js.Dynamic,
                        @named("ConnectService") connectService: ConnectService,
                        @named("MySession") mySession: MySession)
  extends ScopeController {

  private val scope = $scope.asInstanceOf[Scope]
  private var myUpdates = emptyArray[js.Dynamic]
  private var myUpdate: js.Dynamic = null
  private var contact: js.Dynamic = JS()

  /**
   * Selects a specific contact/friend
   */
  $scope.chooseContact = (friend: js.Dynamic) => {
    //g.console.log(s"contact = ${JSON.stringify(friend, null, "\t")}")
    contact = friend
    $scope.getUserInfo(friend.id)
  }

  /**
   * Selects the contact in the player"s friends list
   */
  $scope.chooseFirstContact = () => mySession.fbFriends.headOption foreach ($scope chooseContact _)

  $scope.getContact = () => contact

  $scope.getMyUpdates = () => myUpdates

  $scope.getSelectedUpdate = () => myUpdate

  /**
   * Selects a specific message
   */
  $scope.selectUpdate = (entry: js.Dynamic) => myUpdate = entry

  /**
   * Retrieve a limited set of user profile information for a specific contact/friend
   */
  $scope.getUserInfo = (fbUserId: String) => {
    $scope.startLoading()
    connectService.getUserInfo(fbUserId) onComplete {
      case Success(profile) =>
        $scope.stopLoading()
        contact.profile = profile
      case Failure(e) =>
        $scope.stopLoading()
        g.console.log(s"Failed to retrieve profile for contact ${contact.name}")
        toaster.error(s"Failed to retrieve the user profile for contact ${contact.name}")
    }
  }

  $scope.identifyFacebookFriends = (fbFriends: js.Array[js.Dynamic]) => {
    $scope.startLoading()
    connectService.identifyFacebookFriends(fbFriends) onComplete {
      case Success(_) => $scope.stopLoading()
      case Failure(e) => $scope.stopLoading()
    }
  }

  /**
   * Returns the contacts matching the given search term
   */
  $scope.getContactList = (searchTerm: js.UndefOr[String]) => {
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
  $scope.loadMyUpdates = (userName: String) => loadMyUpdates(userName)

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
   * Composes a new message via pop-up dialog
   */
  $scope.composeMessage = () => composeMessageDialog.popup(JS(success = () => loadMyUpdates(mySession.getUserName())))

  /**
   * Deletes selected messages
   */
  $scope.deleteMessages = (userName: String) => {
    $scope.startLoading()

    // gather the records to delete
    val messageIDs = myUpdates.filter(update => isDefined(update.selected) && update.selected.as[Boolean]).map(_.OID)

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

  /**
   * Selects all currently visible messages
   */
  $scope.selectAll = (checked: Boolean) => myUpdates.foreach(_.selected = checked)

  // watch for changes to the player"s profile
  scope.$watch(mySession.getUserID, () => {
    loadMyUpdates(mySession.getUserName())
    //$scope.chooseFirstContact()
  })

}
