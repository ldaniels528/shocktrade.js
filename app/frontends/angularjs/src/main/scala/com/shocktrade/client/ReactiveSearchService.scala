package com.shocktrade.client

import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * Reactive Search Service
 * @author lawrence.daniels@gmail.com
 */
class ReactiveSearchService($http: Http) extends Service {

  def search(searchTerm: String, maxResults: Int): js.Promise[HttpResponse[js.Array[EntitySearchResult]]] = {
    $http.get(s"/api/search?searchTerm=$searchTerm&maxResults=$maxResults")
  }

}