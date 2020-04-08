package com.shocktrade.client.contest

import com.shocktrade.common.models.contest.{ContestRanking, ContestSearchResult}
import io.scalajs.npm.angularjs.Scope

import scala.scalajs.js

/**
 * Contest CSS Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ContestCssSupport {

  ///////////////////////////////////////////////////////////////////////////
  //          Injected Variables
  ///////////////////////////////////////////////////////////////////////////

  def $scope: ContestCssSupportScope

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getContestStatusClass = (aStatus: js.UndefOr[String]) => aStatus map {
    case "ACTIVE" => "fa fa-circle positive"
    case "CLOSED" => "fa fa-circle negative"
    case "QUEUED" => "fa fa-circle positive"
    case _ => "fa fa-circle null"
  }

  $scope.getDurationClass = (aDuration: js.UndefOr[Int]) => aDuration.map(getDurationClass)

  private def getDurationClass(duration: Int): String = duration match {
    case days if days < 7 => "fa fa-battery-0 cs_battery_0"
    case days if days < 14 => "fa fa-battery-1 cs_battery_1"
    case days if days < 21 => "fa fa-battery-2 cs_battery_2"
    case days if days < 28 => "fa fa-battery-3 cs_battery_3"
    case _ => "fa fa-battery-4 cs_battery_4"
  }

  $scope.getExpandedTrophyIcon = (aRanking: js.UndefOr[ContestRanking]) => aRanking.flatMap(_.rankNum).map {
    case 1 => "fa fa-trophy ds-1st"
    case 2 => "fa fa-trophy ds-2nd"
    case 3 => "fa fa-trophy ds-3rd"
    case _ => "fa fa-trophy ds-nth"
  }

  $scope.getStatusClass = (aContest: js.UndefOr[ContestSearchResult]) => getStatusClass(aContest)

  private def getStatusClass(aContest: js.UndefOr[ContestSearchResult]): String = {
    aContest map {
      case c if c.isEmpty => ""
      case c if c.isFull => "negative"
      case c if !c.isAlmostFull => "positive"
      case c if c.isAlmostFull => "warning"
      case c if c.isActive => "positive"
      case c if c.isClosed => "negative"
      case _ => "null"
    } getOrElse ""
  }

  $scope.trophy = (aPlace: js.UndefOr[String]) => aPlace map trophy

  private def trophy(place: String): String = place match {
    case "1st" => "contests/gold.png"
    case "2nd" => "contests/silver.png"
    case "3rd" => "contests/bronze.png"
    case _ => "status/transparent.png"
  }

}

/**
 * Contest CSS Support Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait ContestCssSupportScope extends js.Object {
  ref: Scope =>

  var getContestStatusClass: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native
  var getDurationClass: js.Function1[js.UndefOr[Int], js.UndefOr[String]] = js.native
  var getExpandedTrophyIcon: js.Function1[js.UndefOr[ContestRanking], js.UndefOr[String]] = js.native
  var getStatusClass: js.Function1[js.UndefOr[ContestSearchResult], String] = js.native
  var trophy: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native

}
