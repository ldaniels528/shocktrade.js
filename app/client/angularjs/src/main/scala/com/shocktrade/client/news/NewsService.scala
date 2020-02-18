package com.shocktrade.client.news

import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
  * News Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NewsService($http: Http) extends Service {

  def getNewsSources: js.Promise[HttpResponse[js.Array[NewsSource]]] = $http.get[js.Array[NewsSource]]("/api/news/sources")

  def getNewsFeed(feedId: String): js.Promise[HttpResponse[js.Array[NewsChannel]]] = $http.get[js.Array[NewsChannel]](s"/api/news/feed/$feedId")

}
