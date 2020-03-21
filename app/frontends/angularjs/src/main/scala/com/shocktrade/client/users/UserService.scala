package com.shocktrade.client.users

import com.shocktrade.client.models.UserProfile
import com.shocktrade.common.models.OperationResult
import com.shocktrade.common.models.quote.Ticker
import com.shocktrade.common.models.user.{NetWorth, OnlineStatus}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * User Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserService($http: Http) extends Service {

  /**
   * Retrieves a user by ID
   * @param userID the given user ID
   */
  def getUserByID(userID: String): js.Promise[HttpResponse[UserProfile]] = $http.get(s"/api/user/$userID")

  /**
   * Retrieves an array of users by ID
   * @param userIDs the given user IDs
   */
  def getUsers(userIDs: js.Array[String]): js.Promise[HttpResponse[js.Array[UserProfile]]] = $http.put(s"/api/users", data = userIDs)

  //////////////////////////////////////////////////////////////////////
  //              Profile Lookup Functions
  //////////////////////////////////////////////////////////////////////

  /**
   * Updates and retrieves the user's net worth
   * @param userID the given user ID
   */
  def getNetWorth(userID: String): js.Promise[HttpResponse[NetWorth]] = $http.get(s"/api/user/$userID/netWorth")

  //////////////////////////////////////////////////////////////////////
  //              Online Status Functions
  //////////////////////////////////////////////////////////////////////

  def getOnlineStatus(userID: String): js.Promise[HttpResponse[OnlineStatus]] = $http.get(s"/api/online/$userID")

  def setIsOnline(userID: String): js.Promise[HttpResponse[js.Any]] = $http.put(s"/api/online/$userID")

  def setIsOffline(userID: String): js.Promise[HttpResponse[js.Any]] = $http.delete(s"/api/online/$userID")

  //////////////////////////////////////////////////////////////////////
  //              Favorite Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.put(s"/api/user/$userID/favorite/$symbol")
  }

  def findFavoriteSymbols(userID: String): js.Promise[HttpResponse[js.Array[Ticker]]] = {
    $http.get(s"/api/user/$userID/favorite")
  }

  def removeFavoriteSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.delete(s"/api/user/$userID/favorite/$symbol")
  }

  //////////////////////////////////////////////////////////////////////
  //              Recent Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addRecentSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.put(s"/api/user/$userID/recent/$symbol")
  }

  def findRecentSymbols(userID: String): js.Promise[HttpResponse[js.Array[Ticker]]] = {
    $http.get(s"/api/user/$userID/recent")
  }

  def removeRecentSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.delete(s"/api/user/$userID/recent/$symbol")
  }

}
