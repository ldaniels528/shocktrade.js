package com.shocktrade.client

import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.Http

import scala.scalajs.js

/**
  * Reactive Search Service
  * @author lawrence.daniels@gmail.com
  */
class ReactiveSearchService($http: Http) extends Service {

  def search(searchTerm: String, maxResults: Int = 20) = {
    $http.get[js.Array[EntitySearchResult]](s"/api/search?searchTerm=$searchTerm&maxResults=$maxResults")
  }

}