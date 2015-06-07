package com.shocktrade.javascript.profile

import biz.enef.angulate.Service
import biz.enef.angulate.core.HttpService

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
 * Connect Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class ConnectService($http: HttpService) extends Service {

  def deleteMessages: js.Function = (messageIDs: js.Array[String]) => deleteMessages_@(messageIDs)

  def deleteMessages_@(messageIDs: js.Array[String]) = $http.delete[js.Dynamic]("/api/updates", messageIDs)

  def getUserInfo: js.Function = (facebookID: String) => getUserInfo_@(facebookID)

  def getUserInfo_@(facebookID: String) = $http.get[js.Dynamic](s"/api/profile/facebook/$facebookID")

  def getUserUpdates: js.Function = (userName: String, limit: Int) => getUserUpdates_@(userName, limit)

  def getUserUpdates_@(userName: String, limit: Int) = $http.get[js.Array[js.Dynamic]](s"/api/updates/$userName/$limit")

  def identifyFacebookFriends: js.Function = (fbFriends: js.Array[js.Dynamic]) => identifyFacebookFriends_@(fbFriends)

  def identifyFacebookFriends_@(fbFriends: js.Array[js.Dynamic]) = $http.post[js.Dynamic]("/api/profile/facebook/friends", fbFriends.map(_.id))


}
