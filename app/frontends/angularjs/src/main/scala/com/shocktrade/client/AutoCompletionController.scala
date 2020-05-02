package com.shocktrade.client

import com.shocktrade.client.discover.QuoteService
import com.shocktrade.common.models.quote.AutoCompleteQuote
import io.scalajs.npm.angularjs.{Controller, Q, Scope}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Represents a symbol auto-completion controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
abstract class AutoCompletionController($scope: AutoCompletionControllerScope, $q: Q, quoteService: QuoteService)
  extends Controller {

  $scope.autoCompleteSymbols = (aSearchTerm: js.UndefOr[String]) => aSearchTerm map { searchTerm =>
    val deferred = $q.defer[js.Array[AutoCompleteQuote]]()
    quoteService.autoCompleteSymbols(searchTerm, maxResults = 10) onComplete {
      case Success(response) => deferred.resolve(response.data)
      case Failure(e) => deferred.reject(e.getMessage)
    }
    deferred.promise
  }

}

/**
 * Auto-Completion Controller Scope
 */
@js.native
trait AutoCompletionControllerScope extends Scope {
  var autoCompleteSymbols: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[js.Array[AutoCompleteQuote]]]] = js.native

}