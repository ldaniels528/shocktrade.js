package com.shocktrade.client

import org.scalajs.angularjs.{Controller, Scope, injected}

import scala.scalajs.js

/**
  * Navigation Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NavigationController($scope: NavigationControllerScope,
                           @injected("MySessionService") mySession: MySessionService)
  extends Controller {

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
