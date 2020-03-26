package com.shocktrade.client.contest

import io.scalajs.npm.angularjs.{Controller, Location, Scope}

import scala.scalajs.js

/**
 * Contest Entry Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ContestEntrySupport[A <: ContestEntrySupportScope] {
  ref: Controller =>

  def $location: Location

  def $scope: A

  ////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.enterGame = (aContestID: js.UndefOr[String]) => aContestID foreach enterGame

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  def enterGame(contestID: String): Unit = $location.path(s"/dashboard/$contestID")

}

/**
 * Contest Entry Support Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait ContestEntrySupportScope extends Scope {
  ref: Scope =>

  // functions
  var enterGame: js.Function1[js.UndefOr[String], Unit] = js.native

}