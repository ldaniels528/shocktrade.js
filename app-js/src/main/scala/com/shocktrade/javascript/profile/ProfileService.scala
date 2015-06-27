package com.shocktrade.javascript.profile

import com.ldaniels528.javascript.angularjs.core.{Http, Service}
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic._
import scala.scalajs.js.annotation.JSExportAll

/**
 * Profile Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class ProfileService($http: Http) extends Service {

  //////////////////////////////////////////////////////////////////////
  //              Profile Lookup Functions
  //////////////////////////////////////////////////////////////////////

  /**
   * Retrieves the current user's profile by FaceBook ID
   */
  def getProfileByFacebookID(facebookID: String) = {
    required("facebookID", facebookID)
    $http.get[js.Dynamic](s"/api/profile/facebook/$facebookID")
  }

  //////////////////////////////////////////////////////////////////////
  //              Online Status Functions
  //////////////////////////////////////////////////////////////////////

  def getOnlineStatus(userID: String) = {
    required("userID", userID)
    $http.get[js.Dynamic](s"/api/online/$userID")
  }

  def setIsOnline(userID: String) = {
    required("userID", userID)
    $http.put[js.Dynamic](s"/api/online/$userID")
  }

  def setIsOffline(userID: String) = {
    required("userID", userID)
    $http.delete[js.Dynamic](s"/api/online/$userID")
  }

  //////////////////////////////////////////////////////////////////////
  //              Exchange Set Functions
  //////////////////////////////////////////////////////////////////////

  @deprecated
  def getExchanges(userID: String) = {
    required("userID", userID)
    $http.get[js.Dynamic](s"/api/profile/$userID/exchanges")
  }

  @deprecated
  def updateExchanges(userID: String, exchanges: js.Array[String]) = {
    required("userID", userID)
    $http.post[js.Dynamic]("/api/exchanges", literal(id = userID, exchanges = exchanges))
  }

  //////////////////////////////////////////////////////////////////////
  //              Favorite Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(userID: String, symbol: String) = {
    required("userID", userID)
    required("symbol", symbol)
    $http.put[js.Dynamic](s"/api/profile/$userID/favorite/$symbol")
  }

  def removeFavoriteSymbol(userID: String, symbol: String) = {
    required("userID", userID)
    required("symbol", symbol)
    $http.delete[js.Dynamic](s"/api/profile/$userID/favorite/$symbol")
  }

  //////////////////////////////////////////////////////////////////////
  //              Recent Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addRecentSymbol(userID: String, symbol: String) = {
    required("userID", userID)
    required("symbol", symbol)
    $http.put[js.Dynamic](s"/api/profile/$userID/recent/$symbol")
  }

  def removeRecentSymbol(userID: String, symbol: String) = {
    required("userID", userID)
    required("symbol", symbol)
    $http.delete[js.Dynamic](s"/api/profile/$userID/recent/$symbol")
  }

}
