package com.shocktrade.javascript.news

import biz.enef.angulate.Service
import com.ldaniels528.javascript.angularjs.core.Http

import scala.scalajs.js

/**
 * News Service
 * @author lawrence.daniels@gmail.com
 */
class NewsService($http: Http) extends Service {

  def getNewsSources = $http.get[js.Array[js.Dynamic]]("/api/news/sources")

  def getNewsFeed(feedId: String) = $http.get[js.Array[js.Dynamic]](s"/api/news/feed/$feedId")

}
