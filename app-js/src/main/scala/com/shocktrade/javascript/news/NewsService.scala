package com.shocktrade.javascript.news

import com.ldaniels528.javascript.angularjs.{ScalaJsHelper, Service}
import com.ldaniels528.javascript.angularjs.core.Http
import ScalaJsHelper._

import scala.scalajs.js

/**
 * News Service
 * @author lawrence.daniels@gmail.com
 */
class NewsService($http: Http) extends Service {

  def getNewsSources = $http.get[js.Array[js.Dynamic]]("/api/news/sources")

  def getNewsFeed(feedId: String) = {
    required("feedId", feedId)
    $http.get[js.Array[js.Dynamic]](s"/api/news/feed/$feedId")
  }

}
