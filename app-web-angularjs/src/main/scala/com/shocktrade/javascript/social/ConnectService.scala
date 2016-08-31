package com.shocktrade.javascript.social

import com.shocktrade.javascript.models.{MyUpdate, UserProfile}
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http
import org.scalajs.nodejs.social.facebook.TaggableFriend

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Connect Service
  * @author lawrence.daniels@gmail.com
  */
@JSExportAll
class ConnectService($http: Http) extends Service {

  def deleteMessages(messageIDs: js.Array[String]) = $http.delete[js.Dynamic]("/api/updates", messageIDs)

  def getUserInfo(facebookID: String) = $http.get[UserProfile](s"/api/profile/facebook/$facebookID")

  def getUserUpdates(userName: String, limit: Int) = $http.get[js.Array[MyUpdate]](s"/api/updates/$userName/$limit")

  def identifyFacebookFriends(fbFriends: js.Array[TaggableFriend]) = {
    $http.post[js.Dynamic]("/api/profile/facebook/friends", fbFriends.map(_.id))
  }

}
