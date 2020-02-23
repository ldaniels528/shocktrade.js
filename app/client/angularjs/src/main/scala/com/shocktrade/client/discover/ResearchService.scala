package com.shocktrade.client.discover

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.ResearchQuote
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
  * Research Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ResearchService($http: Http) extends Service {

  def search(searchOptions: ResearchOptions): js.Promise[HttpResponse[js.Array[ResearchQuote]]] = {
    $http.post[js.Array[ResearchQuote]]("/api/research/search", searchOptions)
  }

}

