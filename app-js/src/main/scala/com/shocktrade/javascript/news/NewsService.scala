package com.shocktrade.javascript.news

import com.shocktrade.javascript.ScalaJsHelper
import ScalaJsHelper._
import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Http

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
