package com.shocktrade.common.api

/**
 * User API
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait UserAPI extends BaseAPI {

  //////////////////////////////////////////////////////////////////////
  //              Basic Functions
  //////////////////////////////////////////////////////////////////////

  def createAccountURL = s"$baseURL/api/user"

  def findUserIconURL(userID: String) = s"$baseURL/api/user/$userID/icon"

  def findUserByIDURL(userID: String) = s"$baseURL/api/user/$userID"

  def findUserByNameURL(username: String) = s"$baseURL/api/user/name/$username"

  def findUsersURL(userIDs: String) = s"$baseURL/api/users?ids=$userIDs"

  //////////////////////////////////////////////////////////////////////
  //              Awards Functions
  //////////////////////////////////////////////////////////////////////

  def findMyAwardsURL(userID: String) = s"$baseURL/api/user/$userID/awards"

  //////////////////////////////////////////////////////////////////////
  //              Authentication Functions
  //////////////////////////////////////////////////////////////////////

  def getCodeURL = s"$baseURL/api/auth/code"

  def loginURL = s"$baseURL/api/auth/login"

  def logoutURL = s"$baseURL/api/auth/logout"

  //////////////////////////////////////////////////////////////////////
  //              Favorite Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addFavoriteSymbolURL(userID: String, symbol: String) = s"$baseURL/api/user/$userID/favorite/$symbol"

  def findFavoriteSymbolsURL(userID: String) = s"$baseURL/api/user/$userID/favorite"

  def removeFavoriteSymbolURL(userID: String, symbol: String) = s"$baseURL/api/user/$userID/favorite/$symbol"

  //////////////////////////////////////////////////////////////////////
  //              Online Status Functions
  /////////////////////////////////////////////////////////////////////

  def getOnlineStatusURL(userID: String) = s"$baseURL/api/online/$userID"

  def getOnlineStatusesURL = s"$baseURL/api/online"

  def setIsOnlineURL(userID: String) = s"$baseURL/api/online/$userID"

  def setIsOfflineURL(userID: String) = s"$baseURL/api/online/$userID"

  //////////////////////////////////////////////////////////////////////
  //              Recent Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addRecentSymbolURL(userID: String, symbol: String) = s"$baseURL/api/user/$userID/recent/$symbol"

  def findRecentSymbolsURL(userID: String) = s"$baseURL/api/user/$userID/recent"

  def removeRecentSymbolURL(userID: String, symbol: String) = s"$baseURL/api/user/$userID/recent/$symbol"

}
