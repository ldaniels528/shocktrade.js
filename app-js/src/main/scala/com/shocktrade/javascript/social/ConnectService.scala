package com.shocktrade.javascript.social

import biz.enef.angulate.Service
import com.ldaniels528.javascript.angularjs.core.Http

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
 * Connect Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class ConnectService($http: Http) extends Service {

  def deleteMessages(messageIDs: js.Array[String]) = $http.delete[js.Dynamic]("/api/updates", messageIDs)

  def getUserInfo(facebookID: String) = $http.get[js.Dynamic](s"/api/profile/facebook/$facebookID")

  def getUserUpdates(userName: String, limit: Int) = $http.get[js.Array[js.Dynamic]](s"/api/updates/$userName/$limit")

  def identifyFacebookFriends(fbFriends: js.Array[js.Dynamic]) = {
    $http.post[js.Dynamic]("/api/profile/facebook/friends", fbFriends.map(_.id))
  }

}
