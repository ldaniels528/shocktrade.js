package com.ldaniels528.javascript.angularjs.extensions

import scala.scalajs.js

/**
 * Route Provider
 * @author lawrence.daniels@gmail.com
 */
trait RouteProvider extends js.Object {

  def when(path: String, route: Route) : RouteProvider = js.native

  def otherwise(params: Route) : RouteProvider = js.native

}
