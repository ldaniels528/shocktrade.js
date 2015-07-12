package com.shocktrade.javascript

import com.github.ldaniels528.scalascript.Controller
import com.github.ldaniels528.scalascript.core.Q
import com.shocktrade.javascript.discover.QuoteService

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Represents a symbol auto-completion controller
 * @author lawrence.daniels@gmail.com
 */
abstract class AutoCompletionController($q: Q, quoteService: QuoteService) extends Controller {

  def autoCompleteSymbols(searchTerm: String)(implicit ec: ExecutionContext) = {
    val deferred = $q.defer[js.Array[js.Dynamic]]()
    quoteService.autoCompleteSymbols(searchTerm, maxResults = 20) onComplete {
      case Success(response) => deferred.resolve(response)
      case Failure(e) => deferred.reject(e.getMessage)
    }
    deferred.promise
  }

}
