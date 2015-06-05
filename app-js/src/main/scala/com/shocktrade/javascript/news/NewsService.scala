package com.shocktrade.javascript.news

import com.greencatsoft.angularjs.core.{HttpPromise, HttpService}
import com.greencatsoft.angularjs.{Factory, Service, injectable}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
 * News Service
 * @author lawrence.daniels@gmail.com
 */
@injectable("NewsService")
class NewsService($http: HttpService) extends Service {
  private val cache = js.Dictionary[HttpPromise]()

  require($http != null, "Missing argument '$http'")

  def loadFeed(feedId: String) = $http.get(s"/api/news/feed/$feedId")

  def getFeed(feedId: String) = cache.getOrElseUpdate(feedId, loadFeed(feedId))

  def getSources = $http.get("/api/news/sources")

}

@injectable("NewsService")
class NewsServiceFactory(http: HttpService) extends Factory[NewsService] {

  override def apply(): NewsService = new NewsService(http)

}