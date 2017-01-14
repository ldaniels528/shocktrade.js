package com.shocktrade.client.profile

import com.shocktrade.client.models.UserProfile
import com.shocktrade.common.forms.ExchangesForm
import com.shocktrade.common.models.user.{NetWorth, OnlineStatus}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.Http
import io.scalajs.nodejs.social.facebook.FacebookProfileResponse

import scala.scalajs.js

/**
  * User Profile Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class UserProfileService($http: Http) extends Service {

  //////////////////////////////////////////////////////////////////////
  //              Profile Lookup Functions
  //////////////////////////////////////////////////////////////////////

  /**
    * Updates and retrieves the user's net worth
    * @param userID the given user ID
    */
  def getNetWorth(userID: String) = {
    $http.get[NetWorth](s"/api/profile/$userID/netWorth")
  }

  /**
    * Retrieves the current user's profile by FaceBook ID
    * @param facebookID the given Facebook ID
    */
  def getProfileByFacebookID(facebookID: String) = {
    $http.get[UserProfile](s"/api/profile/facebook/$facebookID")
  }

  /**
    * Retrieves (or creates) the current user's profile by FaceBook ID
    * @param fbProfile the given [[FacebookProfileResponse Facebook profile]]
    */
  def getProfileViaFacebook(fbProfile: FacebookProfileResponse) = {
    $http.post[UserProfile](s"/api/profile/facebook", data = fbProfile)
  }

  //////////////////////////////////////////////////////////////////////
  //              Online Status Functions
  //////////////////////////////////////////////////////////////////////

  def getOnlineStatus(userID: String) = {
    $http.get[OnlineStatus](s"/api/online/$userID")
  }

  def setIsOnline(userID: String) = {
    $http.put[js.Dynamic](s"/api/online/$userID")
  }

  def setIsOffline(userID: String) = {
    $http.delete[js.Dynamic](s"/api/online/$userID")
  }

  //////////////////////////////////////////////////////////////////////
  //              Exchange Set Functions
  //////////////////////////////////////////////////////////////////////

  @deprecated("Do not use", since = "0.1")
  def getExchanges(userID: String) = {
    $http.get[js.Dynamic](s"/api/profile/$userID/exchanges")
  }

  @deprecated("Do not use", since = "0.1")
  def updateExchanges(userID: String, exchanges: js.Array[String]) = {
    $http.post[js.Dynamic]("/api/exchanges", new ExchangesForm(id = userID, exchanges = exchanges))
  }

  //////////////////////////////////////////////////////////////////////
  //              Favorite Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(userID: String, symbol: String) = {
    $http.put[UserProfile](s"/api/profile/$userID/favorite/$symbol")
  }

  def removeFavoriteSymbol(userID: String, symbol: String) = {
    $http.delete[UserProfile](s"/api/profile/$userID/favorite/$symbol")
  }

  //////////////////////////////////////////////////////////////////////
  //              Recent Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addRecentSymbol(userID: String, symbol: String) = {
    $http.put[UserProfile](s"/api/profile/$userID/recent/$symbol")
  }

  def removeRecentSymbol(userID: String, symbol: String) = {
    $http.delete[UserProfile](s"/api/profile/$userID/recent/$symbol")
  }

}
