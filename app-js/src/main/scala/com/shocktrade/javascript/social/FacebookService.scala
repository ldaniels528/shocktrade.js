package com.shocktrade.javascript.social

import biz.enef.angulate.Service
import com.greencatsoft.angularjs.core.{Promise, Q}
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.annotation.JSExportAll

/**
 * Facebook Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class FacebookService($q: Q) extends Service {
  type CallbackArray = js.Function1[js.Array[js.Dynamic], Unit]
  type CallbackObject = js.Function1[js.Dynamic, Unit]

  // define the API version and App ID
  val appID: String = FacebookInjector.getShockTradeAppID()
  val version = "v2.3"

  // define the Facebook state variables
  var FB: js.Dynamic = null
  var facebookID: String = null
  var auth: js.Dynamic = null
  var accessToken: String = null
  var profile: js.Dynamic = null

  /**
   * Initializes the Facebook service
   */
  def init: js.Function1[js.Dynamic, Promise] = (fbSDK: js.Dynamic) => {
    val deferred = $q.defer()
    FB = fbSDK

    // get the login status
    g.console.log("Retrieving Facebook login status...")
    FB.getLoginStatus((response: js.Dynamic) => {
      g.console.log(s"Facebook login status is ${response.status}")
      if (response.status === "connected") {
        // capture the Facebook login status
        if (isDefined(response.authResponse)) {
          g.console.log("Successfully loaded the Facebook profile...")
          auth = response.authResponse
          facebookID = auth.userID.as[String]
          accessToken = auth.accessToken.as[String]
          g.console.log(s"accessToken = $accessToken")
          deferred.resolve(response)
        }
        else deferred.reject("Facebook response was undefined")
      }
      else deferred.reject(s"Facebook is not connected (status: ${response.status})")
    })
    deferred.promise
  }

  def createFriendList: js.Function1[String, Promise] = { (friendListId: String) =>
    val deferred = $q.defer()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      FB.api(s"/$version/me/$friendListId/members&access_token=$accessToken", { (response: js.Dynamic) =>
        if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response)
        else deferred.reject("Failed to create friends list")
      })
    }
    deferred.promise
  }

  def getFriends(callback: CallbackObject) {
    if (!isDefined(FB)) die("Facebook SDK is not loaded")
    else {
      FB.api(s"/$version/me/friends?access_token=$accessToken", (response: js.Dynamic) => callback(response))
    }
  }

  def getFriendList: js.Function = (listType: js.UndefOr[String]) => {
    val deferred = $q.defer()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      FB.api(s"/$version/me/friendlists?list_type=${listType getOrElse "close_friends"}&access_token=$accessToken", (response: js.Dynamic) => {
        if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response)
        else deferred.reject(s"Failed to retrieve friends list (type $listType)")
      })
    }
    deferred.promise
  }

  def getFriendListMembers: js.Function1[String, Promise] = (friendListId: String) => {
    val deferred = $q.defer()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      FB.api(s"/$version/me/$friendListId/members&access_token=$accessToken", (response: js.Dynamic) => {
        if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response)
        else deferred.reject("Failed to retrieve friend list members")
      })
    }
    deferred.promise
  }

  def getTaggableFriends(callback: CallbackObject) {
    if (!isDefined(FB)) throw new IllegalStateException("Facebook SDK is not loaded")
    else {
      FB.api(s"/$version/me/taggable_friends?access_token=$accessToken", (response: js.Dynamic) => paginatedResults(response, callback))
    }
    ()
  }

  def paginatedResults(response: js.Dynamic, callback: CallbackObject) {
    // perform the callback for this response
    callback(response)

    // if there are more results, recursively extract them
    if (isDefined(response.paging) && isDefined(response.paging.next)) {
      g.console.log(s"Getting next page: ${response.paging.next}")
      FB.api(response.paging.next, (response: js.Dynamic) => paginatedResults(response, callback))
    }
  }

  def getLoginStatus: js.Function0[Promise] = () => {
    val deferred = $q.defer()
    if (!isDefined(FB)) throw new IllegalStateException("Facebook SDK is not loaded")
    else {
      FB.getLoginStatus((response: js.Dynamic) => {
        response.status.asOpt[String] match {
          case Some("connected") =>
            // the user is logged in and has authenticated your app, and response.authResponse supplies
            // the user"s ID, a valid access token, a signed request, and the time the access token
            // and signed request each expire
            g.console.log("User connected... Gathering information...")
            facebookID = response.authResponse.userID.as[String]
            accessToken = response.authResponse.accessToken.as[String]
            auth = response.authResponse
            g.console.log("Completed gathering information.")
            deferred.resolve(response)
          case Some("not_authorized") =>
            // the user is logged in to Facebook, but has not authenticated your app
            deferred.reject("the user is logged in to Facebook, but has not authenticated the app")
          case _ =>
            // the user isn"t logged into Facebook.
            deferred.reject("User is not logged into Facebook")
        }
      })
    }
    deferred.promise
  }

  def getUserProfile: js.Function1[CallbackObject, Unit] = (callback: CallbackObject) => {
    if (!isDefined(FB)) throw new IllegalStateException("Facebook SDK is not loaded")
    else {
      FB.api(s"/$version/me?access_token=${auth.accessToken}", (response: js.Dynamic) => callback(response))
    }
    ()
  }

  def login: js.Function0[Promise] = () => {
    val deferred = $q.defer()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      FB.login((response: js.Dynamic) => {
        if (isDefined(response.authResponse)) {
          auth = response.authResponse
          facebookID = response.authResponse.userID.as[String]
          accessToken = response.authResponse.accessToken.as[String]
          deferred.resolve(response)
        } else {
          deferred.reject("Could not login into Facebook")
        }
      })
    }
    deferred.promise
  }

  def logout: js.Function0[Promise] = () => {
    val deferred = $q.defer()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      FB.logout((response: js.Dynamic) => {
        if (isDefined(response)) {
          auth = null
          deferred.resolve(response)
        } else {
          deferred.reject("Error logging out of Facebook")
        }
      })
    }
    deferred.promise
  }

  def feed: js.Function = (caption: String, link: String) => {
    FB.ui(JS(
      app_id = appID,
      method = "feed",
      link = link,
      caption = caption
    ), (response: js.Dynamic) => {

    })
  }

  def send: js.Function = (message: String, link: String) => {
    FB.ui(JS(
      app_id = appID,
      method = "send",
      link = link
    ), (response: js.Dynamic) => {

    })
  }

  def share: js.Function = (link: String) => {
    FB.ui(JS(
      app_id = appID,
      method = "share",
      href = link
    ), (response: js.Dynamic) => {})
  }

  def getVersion: js.Function0[String] = () => version

}
