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

  $scope.getContestStatusClass = (aStatus: js.UndefOr[String]) => aStatus map {
    case "ACTIVE" => "fa fa-circle positive"
    case "CLOSED" => "fa fa-circle negative"
    case "QUEUED" => "fa fa-circle positive"
    case _ => "fa fa-circle null"
  }

  $scope.getDurationClass = (aDuration: js.UndefOr[Int]) => aDuration.map(getDurationClass)

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  def enterGame(contestID: String): Unit = $location.path(s"/dashboard/$contestID")

  def getDurationClass(duration: Int): String = duration match {
    case days if days < 7 => "fa fa-battery-0 cs_battery_0"
    case days if days < 14 => "fa fa-battery-1 cs_battery_1"
    case days if days < 21 => "fa fa-battery-2 cs_battery_2"
    case days if days < 28 => "fa fa-battery-3 cs_battery_3"
    case _ => "fa fa-battery-4 cs_battery_4"
  }

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
  var getContestStatusClass: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native
  var getDurationClass: js.Function1[js.UndefOr[Int], js.UndefOr[String]] = js.native

}