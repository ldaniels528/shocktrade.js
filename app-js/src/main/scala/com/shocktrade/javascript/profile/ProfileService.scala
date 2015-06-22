package com.shocktrade.javascript.profile

import biz.enef.angulate.Service
import biz.enef.angulate.core.{HttpPromise, HttpService}
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic._
import scala.scalajs.js.annotation.JSExportAll

/**
 * Profile Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class ProfileService($http: HttpService) extends Service {

  //////////////////////////////////////////////////////////////////////
  //              Profile Lookup Functions
  //////////////////////////////////////////////////////////////////////

  /**
   * Retrieves the current user's profile by FaceBook ID
   */
  def getProfileByFacebookID: js.Function1[String, HttpPromise[js.Dynamic]] = (facebookID: String) => {
    required("facebookID", facebookID)
    $http.get[js.Dynamic](s"/api/profile/facebook/$facebookID")
  }

  //////////////////////////////////////////////////////////////////////
  //              Online Status Functions
  //////////////////////////////////////////////////////////////////////

  def getOnlineStatus(userID: String) = $http.get[js.Dynamic](s"/api/online/$userID")

  def setIsOnline(userID: String) = $http.put[js.Dynamic](s"/api/online/$userID")

  def setIsOffline(userID: String) = $http.delete[js.Dynamic](s"/api/online/$userID")

  //////////////////////////////////////////////////////////////////////
  //              Exchange Set Functions
  //////////////////////////////////////////////////////////////////////

  @deprecated
  def getExchanges: js.Function1[String, HttpPromise[js.Dynamic]] = (userID: String) => {
    required("userID", userID)
    $http.get[js.Dynamic](s"/api/profile/$userID/exchanges")
  }

  @deprecated
  def updateExchanges: js.Function2[String, js.Array[String], HttpPromise[js.Dynamic]] = (userID: String, exchanges: js.Array[String]) => {
    required("userID", userID)
    $http.post[js.Dynamic]("/api/exchanges", literal(id = userID, exchanges = exchanges))
  }

  //////////////////////////////////////////////////////////////////////
  //              Favorite Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol: js.Function2[String, String, HttpPromise[js.Dynamic]] = (userID: String, symbol: String) => {
    required("userID", userID)
    required("symbol", symbol)
    $http.put[js.Dynamic](s"/api/profile/$userID/favorite/$symbol")
  }

  def removeFavoriteSymbol: js.Function2[String, String, HttpPromise[js.Dynamic]] = (userID: String, symbol: String) => {
    required("userID", userID)
    required("symbol", symbol)
    $http.delete[js.Dynamic](s"/api/profile/$userID/favorite/$symbol")
  }

  //////////////////////////////////////////////////////////////////////
  //              Recent Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addRecentSymbol: js.Function2[String, String, HttpPromise[js.Dynamic]] = (userID: String, symbol: String) => {
    required("userID", userID)
    required("symbol", symbol)
    $http.put[js.Dynamic](s"/api/profile/$userID/recent/$symbol")
  }

  def removeRecentSymbol: js.Function2[String, String, HttpPromise[js.Dynamic]] = (userID: String, symbol: String) => {
    required("userID", userID)
    required("symbol", symbol)
    $http.delete[js.Dynamic](s"/api/profile/$userID/recent/$symbol")
  }

}
