package com.shocktrade.javascript.social

import biz.enef.angulate.Service
import biz.enef.angulate.core.{HttpPromise, HttpService}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
 * Connect Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class ConnectService($http: HttpService) extends Service {

  def deleteMessages: js.Function1[js.Array[String], HttpPromise[js.Dynamic]] = (messageIDs: js.Array[String]) => {
    $http.delete[js.Dynamic]("/api/updates", messageIDs)
  }

  def getUserInfo: js.Function1[String, HttpPromise[js.Dynamic]] = (facebookID: String) => {
    $http.get[js.Dynamic](s"/api/profile/facebook/$facebookID")
  }

  def getUserUpdates: js.Function2[String, Int, HttpPromise[js.Array[js.Dynamic]]] = (userName: String, limit: Int) => {
    $http.get[js.Array[js.Dynamic]](s"/api/updates/$userName/$limit")
  }

  def identifyFacebookFriends: js.Function1[js.Array[js.Dynamic], HttpPromise[js.Dynamic]] = (fbFriends: js.Array[js.Dynamic]) => {
    $http.post[js.Dynamic]("/api/profile/facebook/friends", fbFriends.map(_.id))
  }

}
