package com.shocktrade.client

import io.scalajs.npm.angularjs.{Controller, Scope}

import scala.scalajs.js

/**
 * Navigation Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NavigationController($scope: NavigationControllerScope) extends Controller {

  $scope.init = () => {
    // do something in the future
  }

}

/**
 * Navigation Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait NavigationControllerScope extends Scope {
  var init: js.Function0[Unit] = js.native

}
