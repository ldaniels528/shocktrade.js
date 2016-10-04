package com.shocktrade.client.profile

import com.shocktrade.common.forms.ExchangesForm
import com.shocktrade.common.models.OnlineStatus
import com.shocktrade.client.models.Profile
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.scalajs.js

/**
  * Profile Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ProfileService($http: Http) extends Service {

  //////////////////////////////////////////////////////////////////////
  //              Profile Lookup Functions
  //////////////////////////////////////////////////////////////////////

  /**
    * Retrieves the current user's profile by FaceBook ID
    */
  def getProfileByFacebookID(facebookID: String) = {
    $http.get[Profile](s"/api/profile/facebook/$facebookID")
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
    $http.put[Profile](s"/api/profile/$userID/favorite/$symbol")
  }

  def removeFavoriteSymbol(userID: String, symbol: String) = {
    $http.delete[Profile](s"/api/profile/$userID/favorite/$symbol")
  }

  //////////////////////////////////////////////////////////////////////
  //              Recent Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addRecentSymbol(userID: String, symbol: String) = {
    $http.put[Profile](s"/api/profile/$userID/recent/$symbol")
  }

  def removeRecentSymbol(userID: String, symbol: String) = {
    $http.delete[Profile](s"/api/profile/$userID/recent/$symbol")
  }

}
