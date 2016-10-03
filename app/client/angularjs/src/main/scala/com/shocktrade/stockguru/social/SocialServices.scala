package com.shocktrade.stockguru.social

import com.shocktrade.common.models.FacebookAppInfo
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.scalajs.js.annotation.JSExportAll

/**
  * Social Networking Setup Services
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@JSExportAll
class SocialServices($http: Http) extends Service {

  def getFacebookAppInfo = $http.get[FacebookAppInfo]("/api/social/facebook")

}
