package com.shocktrade.client.profile

import com.shocktrade.common.forms.FacebookFriendForm
import com.shocktrade.common.models.user.{FriendStatus, User}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.Http
import io.scalajs.social.facebook.TaggableFriend

import scala.scalajs.js

/**
  * Profile Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class UserService($http: Http) extends Service {

  /**
    * Retrieves a user by ID
    * @param userID the given user ID
    */
  def getUserByID(userID: String) = {
    $http.get[User](s"/api/user/$userID")
  }

  /**
    * Retrieves an array of users by ID
    * @param userIDs the given user IDs
    */
  def getUsers(userIDs: js.Array[String]) = {
    $http.put[js.Array[User]](s"/api/users", data = userIDs)
  }

  /**
    * Retrieves the status for a user by the Facebook ID
    * @param friend the given [[TaggableFriend Facebook friend]]
    */
  def getFacebookFriendStatus(friend: TaggableFriend) = {
    $http.post[FriendStatus](s"/api/friend/status", data = new FacebookFriendForm(id = friend.id, name = friend.name))
  }

}
