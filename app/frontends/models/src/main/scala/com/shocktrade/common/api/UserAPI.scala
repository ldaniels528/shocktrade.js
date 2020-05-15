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

  def findUsernameIconURL(username: String) = s"$baseURL/api/username/$username/icon"

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
  //              Online Status Functions
  /////////////////////////////////////////////////////////////////////

  def getOnlineStatusUpdatesURL(since: String) = s"$baseURL/api/online/updates/$since"

  def getOnlineStatusURL(userID: String) = s"$baseURL/api/online/$userID"

  def setIsOnlineURL(userID: String) = s"$baseURL/api/online/$userID"

  def setIsOfflineURL(userID: String) = s"$baseURL/api/online/$userID"

  ///////////////////////////////////////////////////////////////
  //          Administrative Routes
  ///////////////////////////////////////////////////////////////

  def getUpdateUserIconsURL: String = s"$baseURL/api/users/icon/update"

}
