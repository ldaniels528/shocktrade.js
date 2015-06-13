package com.shocktrade.javascript.news

import biz.enef.angulate.Service
import biz.enef.angulate.core.{HttpPromise, HttpService}

import scala.scalajs.js

/**
 * News Service
 * @author lawrence.daniels@gmail.com
 */
class NewsService($http: HttpService) extends Service {

  def getNewsSources: js.Function0[HttpPromise[js.Array[js.Dynamic]]] = () => {
    $http.get[js.Array[js.Dynamic]]("/api/news/sources")
  }

  def getNewsFeed: js.Function1[String, HttpPromise[js.Array[js.Dynamic]]] = (feedId: String) => {
    $http.get[js.Array[js.Dynamic]](s"/api/news/feed/$feedId")
  }

}
