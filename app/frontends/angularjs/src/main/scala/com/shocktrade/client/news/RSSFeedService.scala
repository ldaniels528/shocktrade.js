package com.shocktrade.client.news

import com.shocktrade.common.models.rss.RSSChannel
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * RSS Feed Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RSSFeedService($http: Http) extends Service {

  def getNewsSources: js.Promise[HttpResponse[js.Array[NewsSource]]] = $http.get("/api/news/sources")

  def getNewsFeeds(id: String): js.Promise[HttpResponse[js.Array[RSSChannel]]] = $http.get(s"/api/news/feed/$id")

}
