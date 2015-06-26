package com.ldaniels528.javascript.angularjs.extensions

import scala.scalajs.js

/**
 * Angular.js Route
 * @author lawrence.daniels@gmail.com
 * @see https://docs.angularjs.org/api/ngRoute/provider/$routeProvider
 */
trait Route extends js.Object {
  var controller: js.Any = js.native
  var controllerAs: js.Any = js.native
  var redirectTo: js.Any = js.native
  var reloadOnSearch: js.Any = js.native
  var resolve: js.Dictionary[js.Any] = js.native
  var template: js.Any = js.native
  var templateUrl: js.Any = js.native

}

/**
 * Route
 * @author lawrence.daniels@gmail.com
 */
object Route {

  def apply(controller: js.Any = null,
            controllerAs: js.Any = null,
            resolve: js.Dictionary[js.Any] = null,
            redirectTo: js.Any = null,
            reloadOnSearch: java.lang.Boolean = null,
            template: String = null,
            templateUrl: String = null): Route = {
    val route = new js.Object().asInstanceOf[Route]
    if (controller != null) route.controller = controller
    if (controllerAs != null) route.controllerAs = controllerAs
    if (resolve != null) route.resolve = resolve
    if (redirectTo != null) route.redirectTo = redirectTo
    if (reloadOnSearch != null) route.reloadOnSearch = reloadOnSearch.asInstanceOf[Boolean]
    if (template != null) route.template = template
    if (templateUrl != null) route.templateUrl = templateUrl
    route
  }

}

