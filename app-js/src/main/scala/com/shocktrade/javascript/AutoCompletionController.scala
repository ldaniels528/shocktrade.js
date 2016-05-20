package com.shocktrade.javascript

import com.github.ldaniels528.meansjs.angularjs.{Controller, Q, Scope}
import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.shocktrade.javascript.discover.{AutoCompletedQuote, QuoteService}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Represents a symbol auto-completion controller
  * @author lawrence.daniels@gmail.com
  */
abstract class AutoCompletionController($scope: AutoCompletionControllerScope, $q: Q, quoteService: QuoteService)
  extends Controller {

  $scope.autoCompleteSymbols = (aSearchTerm: js.UndefOr[String]) => aSearchTerm map { searchTerm =>
    val deferred = $q.defer[js.Array[AutoCompletedQuote]]()
    quoteService.autoCompleteSymbols(searchTerm, maxResults = 20) onComplete {
      case Success(response) => deferred.resolve(response)
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
  var autoCompleteSymbols: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[js.Array[AutoCompletedQuote]]]]

}