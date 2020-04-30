package com.shocktrade.client.contest

import com.shocktrade.common.models.Award
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * Award Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AwardService($http: Http) extends Service {

  def findByUser(userID: String): js.Promise[HttpResponse[js.Array[Award]]] = $http.get(s"/api/awards/$userID")

}
