package com.shocktrade.javascript.profile

import biz.enef.angulate.Service
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{ literal, global => g}
import scala.scalajs.js.annotation.JSExportAll

/**
 * Facebook Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class FacebookService($q: js.Dynamic) extends Service {
  var FB: js.Dynamic = null
  var appID: String = null
  var version = "v2.3"
  var userID: String = null
  var auth: js.Dynamic = null
  var accessToken: String = null

  def getVersion: js.Function = () => version

  def createFriendList: js.Function = (friendListId: String) => {
    val deferred = $q.defer()
    FB.api(s"/v2.3/me/$friendListId/members&access_token=$accessToken", (response: js.Dynamic) => {
      if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response) else deferred.reject(response)
    })
    deferred.promise
  }

  def getFriends: js.Function = () => {
    val deferred = $q.defer()
    FB.api("/v2.3/me/friends?access_token=$accessToken", (response: js.Dynamic) => {
      if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response) else deferred.reject(response)
    })
    deferred.promise
  }

  def getTaggableFriends: js.Function = () => {
    val deferred = $q.defer()
    FB.api("/v2.3/me/taggable_friends?access_token=$accessToken", (response: js.Dynamic) => {
      if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response) else deferred.reject(response)
    })
    deferred.promise
  }

  def getFriendList: js.Function = (listType: js.UndefOr[String]) => {
    val deferred = $q.defer()
    FB.api(s"/v2.3/me/friendlists?list_type=${listType getOrElse "close_friends"}&access_token=$accessToken", (response: js.Dynamic) => {
      if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response) else deferred.reject(response)
    })
    deferred.promise
  }

  def getFriendListMembers: js.Function = (friendListId: String) => {
    val deferred = $q.defer()
    FB.api(s"/v2.3/me/$friendListId/members&access_token=$accessToken", (response: js.Dynamic) => {
      if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response) else deferred.reject(response)
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
        deferred.reject(response)
      } else {
        // the user isn"t logged in to Facebook.
        deferred.reject(response)
      }
    })
    deferred.promise
  }

  def getUserProfile: js.Function = () => {
    val deferred = $q.defer()
    FB.api(s"/v2.3/me?access_token=${auth.accessToken}", (response: js.Dynamic) => {
      if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response) else deferred.reject(response)
    })
    deferred.promise
  }

  def login: js.Function = () => {
    g.console.log(s"Performing Facebook login using app ID $appID")
    val deferred = $q.defer()
    FB.login((response: js.Dynamic) => {
      if (isDefined(response.authResponse)) {
        auth = response.authResponse
        userID = response.authResponse.userID.as[String]
        accessToken = response.authResponse.accessToken.as[String]
        deferred.resolve(response)
      } else {
        deferred.reject(response)
      }
    })
    deferred.promise
  }

  def logout: js.Function = () => {
    val deferred = $q.defer()
    FB.logout((response: js.Dynamic) => {
      if (isDefined(response)) {
        auth = null
        deferred.resolve(response)
      } else {
        deferred.reject(response)
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
