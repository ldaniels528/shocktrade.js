package com.shocktrade.javascript.discover

import com.shocktrade.javascript.forms.ResearchSearchOptions
import com.shocktrade.javascript.models.quote.ResearchQuote
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.scalajs.js

/**
  * Research Service
  * @author lawrence.daniels@gmail.com
  */
class ResearchService($http: Http) extends Service {

  def search(searchOptions: ResearchSearchOptions) = {
    $http.post[js.Array[ResearchQuote]]("/api/research/search", searchOptions)
  }

}

