package com.shocktrade.client.users

import com.shocktrade.common.forms.{ExchangesForm, FacebookFriendForm}
import com.shocktrade.common.models.OperationResult
import com.shocktrade.common.models.quote.Ticker
import com.shocktrade.common.models.user.{FriendStatus, NetWorth, OnlineStatus, User}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}
import io.scalajs.social.facebook.TaggableFriend

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
  def getUserByID(userID: String): js.Promise[HttpResponse[User]] = $http.get[User](s"/api/user/$userID")

  /**
   * Retrieves an array of users by ID
   * @param userIDs the given user IDs
   */
  def getUsers(userIDs: js.Array[String]): js.Promise[HttpResponse[js.Array[User]]] = {
    $http.put[js.Array[User]](s"/api/users", data = userIDs)
  }

  /**
   * Retrieves the status for a user by the Facebook ID
   * @param friend the given [[TaggableFriend Facebook friend]]
   */
  def getFacebookFriendStatus(friend: TaggableFriend): js.Promise[HttpResponse[FriendStatus]] = {
    $http.post[FriendStatus](s"/api/friend/status", data = new FacebookFriendForm(id = friend.id, name = friend.name))
  }

  //////////////////////////////////////////////////////////////////////
  //              Profile Lookup Functions
  //////////////////////////////////////////////////////////////////////

  /**
   * Updates and retrieves the user's net worth
   * @param userID the given user ID
   */
  def getNetWorth(userID: String): js.Promise[HttpResponse[NetWorth]] = {
    $http.get[NetWorth](s"/api/user/$userID/netWorth")
  }

  //////////////////////////////////////////////////////////////////////
  //              Online Status Functions
  //////////////////////////////////////////////////////////////////////

  def getOnlineStatus(userID: String): js.Promise[HttpResponse[OnlineStatus]] = {
    $http.get[OnlineStatus](s"/api/online/$userID")
  }

  def setIsOnline(userID: String): js.Promise[HttpResponse[js.Dynamic]] = {
    $http.put[js.Dynamic](s"/api/online/$userID")
  }

  def setIsOffline(userID: String): js.Promise[HttpResponse[js.Dynamic]] = {
    $http.delete[js.Dynamic](s"/api/online/$userID")
  }

  //////////////////////////////////////////////////////////////////////
  //              Exchange Set Functions
  //////////////////////////////////////////////////////////////////////

  @deprecated("Do not use", since = "0.1")
  def getExchanges(userID: String): js.Promise[HttpResponse[js.Dynamic]] = {
    $http.get[js.Dynamic](s"/api/user/$userID/exchanges")
  }

  @deprecated("Do not use", since = "0.1")
  def updateExchanges(userID: String, exchanges: js.Array[String]): js.Promise[HttpResponse[js.Dynamic]] = {
    $http.post[js.Dynamic]("/api/exchanges", new ExchangesForm(id = userID, exchanges = exchanges))
  }

  //////////////////////////////////////////////////////////////////////
  //              Favorite Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.put[OperationResult](s"/api/user/$userID/favorite/$symbol")
  }

  def findFavoriteSymbols(userID: String): js.Promise[HttpResponse[js.Array[Ticker]]] = {
    $http.get[js.Array[Ticker]](s"/api/user/$userID/favorite")
  }

  def removeFavoriteSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.delete[OperationResult](s"/api/user/$userID/favorite/$symbol")
  }

  //////////////////////////////////////////////////////////////////////
  //              Recent Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addRecentSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.put[OperationResult](s"/api/user/$userID/recent/$symbol")
  }

  def findRecentSymbols(userID: String): js.Promise[HttpResponse[js.Array[Ticker]]] = {
    $http.get[js.Array[Ticker]](s"/api/user/$userID/recent")
  }

  def removeRecentSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.delete[OperationResult](s"/api/user/$userID/recent/$symbol")
  }

}
