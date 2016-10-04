package com.shocktrade.client.news

import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * News Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NewsService($http: Http) extends Service {

  def getNewsSources = $http.get[js.Array[NewsSource]]("/api/news/sources")

  def getNewsFeed(feedId: String) = $http.get[js.Array[NewsChannel]](s"/api/news/feed/$feedId")

}
