package com.shocktrade.javascript.profile

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.util.ScalaJsHelper
import ScalaJsHelper._
import com.github.ldaniels528.scalascript.core.Http
import com.shocktrade.javascript.models.{BSONObjectID, OnlinePlayerState, UserProfile}

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
    $http.get[UserProfile](s"/api/profile/facebook/$facebookID")
  }

  //////////////////////////////////////////////////////////////////////
  //              Online Status Functions
  //////////////////////////////////////////////////////////////////////

  def getOnlineStatus(userID: BSONObjectID) = {
    required("userID", userID)
    $http.get[OnlinePlayerState](s"/api/online/${userID.$oid}")
  }

  def setIsOnline(userID: BSONObjectID) = {
    required("userID", userID)
    $http.put[js.Dynamic](s"/api/online/${userID.$oid}")
  }

  def setIsOffline(userID: BSONObjectID) = {
    required("userID", userID)
    $http.delete[js.Dynamic](s"/api/online/${userID.$oid}")
  }

  //////////////////////////////////////////////////////////////////////
  //              Exchange Set Functions
  //////////////////////////////////////////////////////////////////////

  @deprecated
  def getExchanges(userID: BSONObjectID) = {
    required("userID", userID)
    $http.get[js.Dynamic](s"/api/profile/${userID.$oid}/exchanges")
  }

  @deprecated
  def updateExchanges(userID: BSONObjectID, exchanges: js.Array[String]) = {
    required("userID", userID)
    $http.post[js.Dynamic]("/api/exchanges", literal(id = userID.$oid, exchanges = exchanges))
  }

  //////////////////////////////////////////////////////////////////////
  //              Favorite Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addFavoriteSymbol(userID: BSONObjectID, symbol: String) = {
    $http.put[js.Dynamic](s"/api/profile/${userID.$oid}/favorite/$symbol")
  }

  def removeFavoriteSymbol(userID: BSONObjectID, symbol: String) = {
    $http.delete[js.Dynamic](s"/api/profile/${userID.$oid}/favorite/$symbol")
  }

  //////////////////////////////////////////////////////////////////////
  //              Recent Symbols Functions
  //////////////////////////////////////////////////////////////////////

  def addRecentSymbol(userID: BSONObjectID, symbol: String) = {
    $http.put[js.Dynamic](s"/api/profile/${userID.$oid}/recent/$symbol")
  }

  def removeRecentSymbol(userID: BSONObjectID, symbol: String) = {
    $http.delete[js.Dynamic](s"/api/profile/${userID.$oid}/recent/$symbol")
  }

}
