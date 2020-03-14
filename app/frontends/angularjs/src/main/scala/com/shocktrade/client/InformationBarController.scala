package com.shocktrade.client

import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.{Controller, Q, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Information Bar Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class InformationBarController($scope: InformationBarScope, $q: Q,
                               @injected("ReactiveSearchService") reactiveSearchSvc: ReactiveSearchService,
                               @injected("WebSocketService") webSocket: WebSocketService) extends Controller {

  $scope.init = () => {
    console.log(s"${getClass.getSimpleName} is initializing...")
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.autoCompleteSearch = (searchTerm: String) => {
    val deferred = $q.defer[js.Array[EntitySearchResult]]()
    reactiveSearchSvc.search(searchTerm, maxResults = 20) onComplete {
      case Success(response) => deferred.resolve(response.data)
      case Failure(e) => deferred.reject(e.displayMessage)
    }
    deferred.promise
  }

  $scope.formatSearchResult = (aResult: js.UndefOr[EntitySearchResult]) => for {
    result <- aResult
    name <- result.name
  } yield name

  $scope.isWebSocketConnected = () => webSocket.isConnected

  $scope.onSelectedItem = (item: js.UndefOr[js.Any], aModel: js.UndefOr[EntitySearchResult], label: js.UndefOr[String]) => {
    for {
      model <- aModel
      entity <- model.`type`
      modelId <- model._id
    } {
      console.log(s"Handling $entity $label")
      entity match {
        case _ =>
          console.warn(s"Entity type '$entity' was unhandled")
      }
    }
  }

}

/**
 * Information Bar Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait InformationBarScope extends RootScope {
  var init: js.Function0[Unit] = js.native
  var autoCompleteSearch: js.Function1[String, js.Promise[js.Array[EntitySearchResult]]] = js.native
  var formatSearchResult: js.Function1[js.UndefOr[EntitySearchResult], js.UndefOr[String]] = js.native
  var isWebSocketConnected: js.Function0[Boolean] = js.native
  var onSelectedItem: js.Function3[js.UndefOr[js.Any], js.UndefOr[EntitySearchResult], js.UndefOr[String], Unit] = js.native

}