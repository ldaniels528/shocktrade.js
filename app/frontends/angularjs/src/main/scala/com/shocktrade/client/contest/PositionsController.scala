package com.shocktrade.client.contest

import com.shocktrade.client.contest.PositionsController.PositionsControllerScope
import com.shocktrade.common.models.contest.Position
import io.scalajs.npm.angularjs.{Controller, Scope}

import scala.scalajs.js

/**
 * Positions Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PositionsController {
  ref: Controller =>

  def $scope: PositionsControllerScope

  /////////////////////////////////////////////////////////////////////
  //          Position Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getPositions = () => $scope.positions

}

/**
 * Positions Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PositionsController {

  /**
   * Positions Controller Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait PositionsControllerScope extends Scope {
    ref: Scope =>

    // functions
    var getPositions: js.Function0[js.UndefOr[js.Array[Position]]] = js.native

    // variables
    var positions: js.UndefOr[js.Array[Position]] = js.native
  }

}