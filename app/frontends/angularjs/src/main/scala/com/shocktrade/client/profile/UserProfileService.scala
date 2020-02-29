package com.shocktrade.client.profile

import com.shocktrade.client.models.UserProfile
import com.shocktrade.common.forms.ExchangesForm
import com.shocktrade.common.models.user.{NetWorth, OnlineStatus}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

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
  def getNetWorth(userID: String): js.Promise[HttpResponse[NetWorth]] = {
    $http.get[NetWorth](s"/api/profile/$userID/netWorth")
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
    $http.get[js.Dynamic](s"/api/profile/$userID/exchanges")
  }

  @deprecated("Do not use", since = "0.1")
  def updateExchanges(userID: String, exchanges: js.Array[String]): js.Promise[HttpResponse[js.Dynamic]] = {
    $http.post[js.Dynamic]("/api/exchanges", new ExchangesForm(id = userID, exchanges = exchanges))
  }

  //////////////////////////////////////////////////////////////////////
  //              Favorite Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(userID: String, symbol: String): js.Promise[HttpResponse[UserProfile]] = {
    $http.put[UserProfile](s"/api/profile/$userID/favorite/$symbol")
  }

  def removeFavoriteSymbol(userID: String, symbol: String): js.Promise[HttpResponse[UserProfile]] = {
    $http.delete[UserProfile](s"/api/profile/$userID/favorite/$symbol")
  }

  //////////////////////////////////////////////////////////////////////
  //              Recent Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addRecentSymbol(userID: String, symbol: String): js.Promise[HttpResponse[UserProfile]] = {
    $http.put[UserProfile](s"/api/profile/$userID/recent/$symbol")
  }

  def removeRecentSymbol(userID: String, symbol: String): js.Promise[HttpResponse[UserProfile]] = {
    $http.delete[UserProfile](s"/api/profile/$userID/recent/$symbol")
  }

}
