package com.shocktrade.javascript.profile

import com.shocktrade.javascript.forms.ExchangesForm
import com.shocktrade.javascript.models.{OnlinePlayerState, UserProfile}
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.scalajs.js

/**
  * Profile Service
  * @author lawrence.daniels@gmail.com
  */
class ProfileService($http: Http) extends Service {

  //////////////////////////////////////////////////////////////////////
  //              Profile Lookup Functions
  //////////////////////////////////////////////////////////////////////

  /**
    * Retrieves the current user's profile by FaceBook ID
    */
  def getProfileByFacebookID(facebookID: String) = {
    $http.get[UserProfile](s"/api/profile/facebook/$facebookID")
  }

  //////////////////////////////////////////////////////////////////////
  //              Online Status Functions
  //////////////////////////////////////////////////////////////////////

  def getOnlineStatus(userID: String) = {
    $http.get[OnlinePlayerState](s"/api/online/$userID")
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

  @deprecated
  def getExchanges(userID: String) = {
    $http.get[js.Dynamic](s"/api/profile/$userID/exchanges")
  }

  @deprecated
  def updateExchanges(userID: String, exchanges: js.Array[String]) = {
    $http.post[js.Dynamic]("/api/exchanges", new ExchangesForm(id = userID, exchanges = exchanges))
  }

  //////////////////////////////////////////////////////////////////////
  //              Favorite Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(userID: String, symbol: String) = {
    $http.put[js.Dynamic](s"/api/profile/$userID/favorite/$symbol")
  }

  def removeFavoriteSymbol(userID: String, symbol: String) = {
    $http.delete[js.Dynamic](s"/api/profile/$userID/favorite/$symbol")
  }

  //////////////////////////////////////////////////////////////////////
  //              Recent Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addRecentSymbol(userID: String, symbol: String) = {
    $http.put[js.Dynamic](s"/api/profile/$userID/recent/$symbol")
  }

  def removeRecentSymbol(userID: String, symbol: String) = {
    $http.delete[js.Dynamic](s"/api/profile/$userID/recent/$symbol")
  }

}
