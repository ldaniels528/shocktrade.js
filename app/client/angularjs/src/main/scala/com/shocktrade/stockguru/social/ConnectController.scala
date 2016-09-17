package com.shocktrade.stockguru.social

import com.shocktrade.common.models.{MyUpdate, Profile}
import com.shocktrade.stockguru.ScopeEvents._
import com.shocktrade.stockguru.dialogs.ComposeMessageDialog
import com.shocktrade.stockguru.{GlobalLoading, MySessionService}
import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.dom.console
import org.scalajs.nodejs.social.facebook.TaggableFriend
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
  * Connect Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ConnectController($scope: ConnectScope, toaster: Toaster,
                        @injected("ComposeMessageDialog") messageDialog: ComposeMessageDialog,
                        @injected("ConnectService") connectService: ConnectService,
                        @injected("MySessionService") mySession: MySessionService)
  extends Controller with GlobalLoading {

  private var myUpdates = emptyArray[MyUpdate]
  private var myUpdate: js.UndefOr[MyUpdate] = js.undefined
  private var contact: js.UndefOr[Contact] = js.undefined

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.chooseContact = (aFriend: js.UndefOr[TaggableFriend]) => {
    //console.log(s"contact = ${JSON.stringify(friend, null, "\t")}")
    this.contact = aFriend.asInstanceOf[Contact]
    $scope.getUserInfo(aFriend.map(_.id))
  }

  $scope.chooseFirstContact = () => $scope.chooseContact(mySession.fbFriends_?.headOption.orUndefined)

  /**
    * Composes a new message via pop-up dialog
    */
  $scope.composeMessage = () => {
    messageDialog.popup() onComplete {
      case Success(response) => $scope.loadMyUpdates(mySession.getUserName)
      case Failure(e) =>
        toaster.error(e.getMessage)
    }
  }

  $scope.getContact = () => contact

  $scope.getFriends = () => mySession.fbFriends_?

  /**
    * Deletes selected messages
    */
  $scope.deleteMessages = (aUserName: js.UndefOr[String]) => aUserName foreach { userName =>
    // gather the records to delete
    val messageIDs = myUpdates.filter(_.selected.isTrue).flatMap(_._id.toOption)
    console.log(s"messageIDs = ${JSON.stringify(messageIDs)}")

    // delete the records
    if (messageIDs.nonEmpty) {
      asyncLoading($scope)(connectService.deleteMessages(messageIDs)) onComplete {
        case Success(response) =>
          $scope.loadMyUpdates(userName)
        case Failure(e) =>
          toaster.error("Failed to delete message")
      }
    }
    else {
      toaster.error("No message(s) selected")
    }
  }

  $scope.getMyUpdates = () => myUpdates

  $scope.getSelectedUpdate = () => myUpdate

  /**
    * Selects a specific message
    */
  $scope.selectUpdate = (entry: js.UndefOr[MyUpdate]) => myUpdate = entry

  $scope.getUserInfo = (aFbUserId: js.UndefOr[String]) => aFbUserId foreach { fbUserId =>
    setUserInfo(fbUserId)
  }

  $scope.identifyFacebookFriends = (aFriends: js.UndefOr[js.Array[TaggableFriend]]) => aFriends foreach { fbFriends =>
    asyncLoading($scope)(connectService.identifyFacebookFriends(fbFriends))
    ()
  }

  $scope.getContactList = (aSearchTerm: js.UndefOr[String]) => {
    // TODO reinstate search
    /*
    console.log(s"searchTerm = $searchTerm (${Option(searchTerm).map(_.getClass.getName).orNull})")
    searchTerm foreach { mySearchTerm =>
      val fbFriends = mySession.fbFriends
      if (!Option(searchTerm).exists(_.nonEmpty)) fbFriends.take(40)
      else {
        val term = mySearchTerm.trim.toLowerCase
        fbFriends.filter(friend => isDefined(friend.name) && friend.name.as[String].toLowerCase.contains(term))
      }
    }*/
    mySession.fbFriends_?
  }

  $scope.loadMyUpdates = (aUserName: js.UndefOr[String]) => aUserName foreach { userName =>
    if (userName.nonEmpty) {
      asyncLoading($scope)(connectService.getUserUpdates(userName, 50)) onComplete {
        case Success(data) =>
          myUpdates = data
          myUpdate = null
          data.foreach(_.selected = false)
        case Failure(e) =>
          console.error(s"Failed to load Connect: ${e.getMessage}")
          toaster.error("Failed to load Connect")
      }
    }
  }

  /**
    * Selects all currently visible messages
    */
  $scope.selectAll = (checked: js.UndefOr[Boolean]) => myUpdates.foreach(_.selected = checked)

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  /**
    * Retrieve a limited set of user profile information for a specific contact/friend
    */
  private def setUserInfo(fbUserId: String) = {
    asyncLoading($scope)(connectService.getUserInfo(fbUserId)) onComplete {
      case Success(profile) =>
        contact.foreach(_.profile = profile)
      case Failure(e) =>
        console.log(s"Failed to retrieve profile for contact ${contact.map(_.name)}")
        toaster.error(s"Failed to retrieve the user profile for contact ${contact.map(_.name)}")
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Event Listeners
  /////////////////////////////////////////////////////////////////////////////

  /**
    * Listen for changes to the player's profile
    */
  $scope.onUserProfileChanged((_, profile) => $scope.loadMyUpdates(mySession.getUserName))

  /**
    * Listen for changes to the player's profile
    */
  $scope.onUserProfileUpdated((_, profile) => $scope.loadMyUpdates(mySession.getUserName))

}

/**
  * Connect Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait ConnectScope extends Scope {
  // functions
  var chooseContact: js.Function1[js.UndefOr[TaggableFriend], Unit]
  var chooseFirstContact: js.Function0[Unit]
  var composeMessage: js.Function0[Unit]
  var getContact: js.Function0[js.UndefOr[TaggableFriend]]
  var getFriends: js.Function0[js.Array[TaggableFriend]]
  var deleteMessages: js.Function1[js.UndefOr[String], Unit]
  var getMyUpdates: js.Function0[js.Array[MyUpdate]]
  var getSelectedUpdate: js.Function0[js.UndefOr[MyUpdate]]
  var selectUpdate: js.Function1[js.UndefOr[MyUpdate], Unit]
  var getUserInfo: js.Function1[js.UndefOr[String], Unit]
  var identifyFacebookFriends: js.Function1[js.UndefOr[js.Array[TaggableFriend]], Unit]
  var getContactList: js.Function1[js.UndefOr[String], js.Array[TaggableFriend]]
  var loadMyUpdates: js.Function1[js.UndefOr[String], Unit]
  var selectAll: js.Function1[js.UndefOr[Boolean], Unit]

}

/**
  * Contact Model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait Contact extends TaggableFriend {
  //  var name: js.UndefOr[String] = js.native
  var profile: js.UndefOr[Profile] = js.native
}