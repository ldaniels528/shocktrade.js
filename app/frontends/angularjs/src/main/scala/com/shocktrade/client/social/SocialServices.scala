package com.shocktrade.client.social

import com.shocktrade.common.models.FacebookAppInfo
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.Http

import scala.scalajs.js.annotation.JSExportAll

/**
 * Social Networking Setup Services
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@JSExportAll
class SocialServices($http: Http) extends Service {

  def getFacebookAppInfo = $http.get[FacebookAppInfo]("/api/social/facebook")

}
