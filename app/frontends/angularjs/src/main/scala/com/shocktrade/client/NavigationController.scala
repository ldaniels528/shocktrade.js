package com.shocktrade.client

import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.Controller

import scala.scalajs.js

/**
 * Navigation Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NavigationController($scope: NavigationControllerScope) extends Controller {

  $scope.initNav = () => {
    console.info(s"Initializing ${getClass.getSimpleName}...")
  }

}

/**
 * Navigation Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait NavigationControllerScope extends RootScope {
  var initNav: js.Function0[Unit] = js.native

}
