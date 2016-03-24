package com.shocktrade.javascript.discover

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
 * Research Service
 * @author lawrence.daniels@gmail.com
 */
class ResearchService($http: Http) extends Service {

  def search(searchOptions: js.Dynamic)(implicit ec: ExecutionContext) = {
    required("searchOptions", searchOptions)
    $http.post[js.Array[js.Dynamic]]("/api/research/search", searchOptions)
  }

}
