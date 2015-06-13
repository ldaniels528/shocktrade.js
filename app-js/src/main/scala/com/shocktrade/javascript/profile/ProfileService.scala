package com.shocktrade.javascript.profile

import biz.enef.angulate.Service
import biz.enef.angulate.core.{HttpPromise, HttpService}
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.annotation.JSExportAll

/**
 * Profile Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class ProfileService($http: HttpService) extends Service {

  /**
   * Retrieves the current user's profile by FaceBook ID
   */
  def getProfileByFacebookID: js.Function1[String, HttpPromise[js.Dynamic]] = (facebookID: String) => {
    required("facebookID", facebookID)
    $http.get[js.Dynamic](s"/api/profile/facebook/$facebookID")
  }

  def getExchanges: js.Function = (profileID: String) => {
    required("profileID", profileID)
    $http.get(s"/api/profile/$profileID/exchanges")
  }

  def updateExchanges: js.Function = (profileID: String, exchanges: js.Array[String]) => {
    required("profileID", profileID)
    $http.post("/api/exchanges", literal(id = profileID, exchanges = exchanges))
  }

}
