package com.shocktrade.javascript.discover

import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
 * Research Service
 * @author lawrence.daniels@gmail.com
 */
class ResearchService($http: Http) extends Service {

  def search(searchOptions: ResearchSearchOptions)(implicit ec: ExecutionContext) = {
    $http.post[js.Array[ResearchQuote]]("/api/research/search", searchOptions)
  }

}

/**
  * Research Search Options
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ResearchSearchOptions extends js.Object {
  var sortBy: js.UndefOr[String]
  var reverse: js.UndefOr[Boolean]
  var maxResults: js.UndefOr[Int]
}

/**
  * Research Search Options Companion Object
  * @author lawrence.daniels@gmail.com
  */
object ResearchSearchOptions {

  def apply(sortBy: js.UndefOr[String] = js.undefined,
            reverse: Boolean = false,
            maxResults: Int) = {
    val options = New[ResearchSearchOptions]
    options.sortBy = sortBy
    options.reverse = reverse
    options.maxResults = maxResults
    options
  }

}

/**
  * Research Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ResearchQuote extends js.Object {
  var symbol: js.UndefOr[String]
  var exchange: js.UndefOr[String]
  var market: js.UndefOr[String]
}