package com.shocktrade.javascript.profile

import biz.enef.angulate.Service
import com.greencatsoft.angularjs.core.Q
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal}
import scala.scalajs.js.annotation.JSExportAll

/**
 * Facebook Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class FacebookService($q: Q) extends Service {
  type CallbackArray = js.Function1[js.Array[js.Dynamic], Unit]
  type CallbackObject = js.Function1[js.Dynamic, Unit]

  var FB: js.Dynamic = null
  var appID: String = null
  var version = "v2.3"
  var userID: String = null
  var auth: js.Dynamic = null
  var accessToken: String = null

  def getVersion: js.Function = () => version

  def createFriendList: js.Function = { (friendListId: String) =>
    val deferred = $q.defer()
    FB.api(s"/v2.3/me/$friendListId/members&access_token=$accessToken", { (response: js.Dynamic) =>
      if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response) else deferred.reject("Failed to create friends list")
    })
    deferred.promise
  }

  def getFriends: js.Function = () => {
    val deferred = $q.defer()
    FB.api(s"/v2.3/me/friends?access_token=$accessToken", (response: js.Dynamic) => {
      if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response) else deferred.reject("Failed to retrieve friends list")
    })
    deferred.promise
  }

  def getTaggableFriends: js.Function = (callback: CallbackObject) => getTaggableFriends_@(callback)

  def getTaggableFriends_@(callback: CallbackObject) = {
    FB.api(s"/v2.3/me/taggable_friends?access_token=$accessToken", (response: js.Dynamic) => callback(response))
  }

  def getFriendList: js.Function = (listType: js.UndefOr[String]) => {
    val deferred = $q.defer()
    FB.api(s"/v2.3/me/friendlists?list_type=${listType getOrElse "close_friends"}&access_token=$accessToken", (response: js.Dynamic) => {
      if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response) else deferred.reject(s"Failed to retrieve friends list (type $listType)")
    })
    deferred.promise
  }

  def getFriendListMembers: js.Function = (friendListId: String) => {
    val deferred = $q.defer()
    FB.api(s"/v2.3/me/$friendListId/members&access_token=$accessToken", (response: js.Dynamic) => {
      if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response) else deferred.reject("Failed to retrieve friend list members")
    })
    deferred.promise
  }

  def getLoginStatus: js.Function = () => {
    val deferred = $q.defer()
    FB.getLoginStatus((response: js.Dynamic) => {
      if (response.status === "connected") {
        // the user is logged in and has authenticated your app, and response.authResponse supplies
        // the user"s ID, a valid access token, a signed request, and the time the access token
        // and signed request each expire
        userID = response.authResponse.userID.as[String]
        accessToken = response.authResponse.accessToken.as[String]
        auth = response.authResponse
        deferred.resolve(response)
      } else if (response.status === "not_authorized") {
        // the user is logged in to Facebook, but has not authenticated your app
        deferred.reject("the user is logged in to Facebook, but has not authenticated the app")
      } else {
        // the user isn"t logged into Facebook.
        deferred.reject("User is not logged into Facebook")
      }
    })
    deferred.promise
  }

  def getUserProfile: js.Function = (callback: CallbackObject) => getUserProfile_@(callback)

  def getUserProfile_@(callback: CallbackObject) {
    FB.api(s"/v2.3/me?access_token=${auth.accessToken}", (response: js.Dynamic) => callback.apply(response))
  }

  def login: js.Function = () => login_@

  def login_@ = {
    g.console.log(s"Performing Facebook login using app ID $appID")
    val deferred = $q.defer()
    FB.login((response: js.Dynamic) => {
      if (isDefined(response.authResponse)) {
        auth = response.authResponse
        userID = response.authResponse.userID.as[String]
        accessToken = response.authResponse.accessToken.as[String]
        deferred.resolve(response)
      } else {
        deferred.reject("Could not login into Facebook")
      }
    })
    deferred.promise
  }

  def logout: js.Function = () => logout_@

  def logout_@ = {
    val deferred = $q.defer()
    FB.logout((response: js.Dynamic) => {
      if (isDefined(response)) {
        auth = null
        deferred.resolve(response)
      } else {
        deferred.reject("Error logging out of Facebook")
      }
    })
    deferred.promise
  }

  def feed: js.Function = (caption: String, link: String) => {
    FB.ui(literal(
      app_id = appID,
      method = "feed",
      link = link,
      caption = caption
    ), (response: js.Dynamic) => {

    })
  }

  def send: js.Function = (message: String, link: String) => {
    FB.ui(literal(
      app_id = appID,
      method = "send",
      link = link
    ), (response: js.Dynamic) => {

    })
  }

  def share: js.Function = (link: String) => {
    FB.ui(literal(
      app_id = appID,
      method = "share",
      href = link
    ), (response: js.Dynamic) => {})
  }

}
