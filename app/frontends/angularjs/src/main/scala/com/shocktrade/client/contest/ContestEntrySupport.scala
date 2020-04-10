package com.shocktrade.client.contest

import io.scalajs.npm.angularjs.{Location, Scope}

import scala.scalajs.js

/**
 * Contest Entry Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ContestEntrySupport {

  ///////////////////////////////////////////////////////////////////////////
  //          Injected Variables
  ///////////////////////////////////////////////////////////////////////////

  def $location: Location

  def $scope: ContestEntrySupportScope

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.enterGame = (aContestID: js.UndefOr[String]) => aContestID map enterGame

  def enterGame(contestID: String): Location = $location.path(s"/dashboard/$contestID")

}

/**
 * Contest Entry Support Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait ContestEntrySupportScope extends js.Object {
  ref: Scope =>

  // functions
  var enterGame: js.Function1[js.UndefOr[String], js.UndefOr[Location]] = js.native

}