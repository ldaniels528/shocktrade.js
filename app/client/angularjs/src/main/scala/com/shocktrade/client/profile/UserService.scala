package com.shocktrade.client.profile

import com.shocktrade.client.models.UserProfile
import com.shocktrade.common.models.user.User
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

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

}
