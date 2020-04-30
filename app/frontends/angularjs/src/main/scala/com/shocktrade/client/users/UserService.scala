package com.shocktrade.client.users

import com.shocktrade.common.api.UserAPI
import com.shocktrade.common.auth.{AuthenticationCode, AuthenticationForm, AuthenticationResponse}
import com.shocktrade.common.forms.SignUpForm
import com.shocktrade.common.models.OperationResult
import com.shocktrade.common.models.quote.Ticker
import com.shocktrade.common.models.user.{OnlineStatus, UserProfile}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * User Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserService($http: Http) extends Service with UserAPI {

  //////////////////////////////////////////////////////////////////////
  //              Basic Functions
  //////////////////////////////////////////////////////////////////////

  /**
   * Creates a new user account
   * @param form the given [[SignUpForm]]
   * @return the promise of a user profile
   */
  def createAccount(form: SignUpForm): js.Promise[HttpResponse[UserProfile]] = $http.post[UserProfile](createAccountURL, form)

  /**
   * Retrieves a user by ID
   * @param userID the given user ID
   */
  def findUserByID(userID: String): js.Promise[HttpResponse[UserProfile]] = $http.get(findUserByIDURL(userID))

  /**
   * Retrieves an array of users by ID
   * @param userIDs the given user IDs
   */
  def findUsers(userIDs: js.Array[String]): js.Promise[HttpResponse[js.Array[UserProfile]]] = $http.get(findUsersURL(userIDs.mkString("+")))

  //////////////////////////////////////////////////////////////////////
  //              Awards Functions
  //////////////////////////////////////////////////////////////////////

  /**
   * Retrieves the collection of awards by ID
   * @param userID the given user ID
   */
  def findMyAwards(userID: String): js.Promise[HttpResponse[js.Array[String]]] = $http.get(findMyAwardsURL(userID))

  //////////////////////////////////////////////////////////////////////
  //              Authentication Functions
  //////////////////////////////////////////////////////////////////////

  def getCode: js.Promise[HttpResponse[AuthenticationCode]] = $http.get(getCodeURL)

  def login(form: AuthenticationForm): js.Promise[HttpResponse[UserProfile]] = $http.post(url = loginURL, data = form)

  def logout(): js.Promise[HttpResponse[AuthenticationResponse]] = $http.post(logoutURL)

  //////////////////////////////////////////////////////////////////////
  //              Online Status Functions
  //////////////////////////////////////////////////////////////////////

  def getOnlineStatusUpdates(since: Double): js.Promise[HttpResponse[js.Array[OnlineStatus]]] = $http.get(getOnlineStatusUpdatesURL(since.toString))

  def getOnlineStatus(userID: String): js.Promise[HttpResponse[OnlineStatus]] = $http.get(getOnlineStatusURL(userID))

  def setIsOnline(userID: String): js.Promise[HttpResponse[js.Any]] = $http.put(setIsOnlineURL(userID))

  def setIsOffline(userID: String): js.Promise[HttpResponse[js.Any]] = $http.delete(setIsOfflineURL(userID))

  //////////////////////////////////////////////////////////////////////
  //              Favorite Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.put(addFavoriteSymbolURL(userID, symbol))
  }

  def findFavoriteSymbols(userID: String): js.Promise[HttpResponse[js.Array[Ticker]]] = {
    $http.get(findFavoriteSymbolsURL(userID))
  }

  def removeFavoriteSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.delete(removeFavoriteSymbolURL(userID, symbol))
  }

  //////////////////////////////////////////////////////////////////////
  //              Recent Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addRecentSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.put(addRecentSymbolURL(userID, symbol))
  }

  def findRecentSymbols(userID: String): js.Promise[HttpResponse[js.Array[Ticker]]] = {
    $http.get(findRecentSymbolsURL(userID))
  }

  def removeRecentSymbol(userID: String, symbol: String): js.Promise[HttpResponse[OperationResult]] = {
    $http.delete(removeRecentSymbolURL(userID, symbol))
  }

}
