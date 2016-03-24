package com.shocktrade.javascript.news

import com.github.ldaniels528.scalascript.util.ScalaJsHelper
import ScalaJsHelper._
import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Http
import com.shocktrade.javascript.models.{BSONObjectID, NewsFeed, NewsSource}

import scala.scalajs.js

/**
 * News Service
 * @author lawrence.daniels@gmail.com
 */
class NewsService($http: Http) extends Service {

  def getNewsSources = $http.get[js.Array[NewsSource]]("/api/news/sources")

  def getNewsFeed(feedId: BSONObjectID) = {
    required("feedId", feedId)
    $http.get[js.Array[NewsFeed]](s"/api/news/feed/${feedId.$oid}")
  }

}
