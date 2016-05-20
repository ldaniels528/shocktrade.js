package com.shocktrade.javascript.social

import com.github.ldaniels528.meansjs.angularjs.Service
import com.github.ldaniels528.meansjs.angularjs.http.Http
import com.github.ldaniels528.meansjs.social.facebook.TaggableFriend
import com.shocktrade.javascript.models.{BSONObjectID, MyUpdate, UserProfile}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
 * Connect Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class ConnectService($http: Http) extends Service {

  def deleteMessages(messageIDs: js.Array[BSONObjectID]) = $http.delete[js.Dynamic]("/api/updates", messageIDs.map(_.$oid))

  def getUserInfo(facebookID: String) = $http.get[UserProfile](s"/api/profile/facebook/$facebookID")

  def getUserUpdates(userName: String, limit: Int) = $http.get[js.Array[MyUpdate]](s"/api/updates/$userName/$limit")

  def identifyFacebookFriends(fbFriends: js.Array[TaggableFriend]) = {
    $http.post[js.Dynamic]("/api/profile/facebook/friends", fbFriends.map(_.id))
  }

}
