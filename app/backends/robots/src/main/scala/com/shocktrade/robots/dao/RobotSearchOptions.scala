package com.shocktrade.robots.dao

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

class RobotSearchOptions(val robotName: js.UndefOr[String] = js.undefined,
                         val isActive: js.UndefOr[Int] = js.undefined,
                         val contestID: js.UndefOr[String] = js.undefined,
                         val maxResults: js.UndefOr[Int] = js.undefined,
                         val portfolioID: js.UndefOr[String] = js.undefined,
                         val userID: js.UndefOr[String] = js.undefined) extends js.Object

object RobotSearchOptions {

  final implicit class RobotSearchOptionsEnrich(val options: RobotSearchOptions) extends AnyVal {

    @inline
    def copy(robotName: js.UndefOr[String] = js.undefined,
             isActive: js.UndefOr[Int] = js.undefined,
             contestID: js.UndefOr[String] = js.undefined,
             maxResults: js.UndefOr[Int] = js.undefined,
             portfolioID: js.UndefOr[String] = js.undefined,
             userID: js.UndefOr[String] = js.undefined): RobotSearchOptions = {
      new RobotSearchOptions(
        robotName = robotName ?? options.robotName,
        isActive = isActive ?? options.isActive,
        contestID = contestID ?? options.contestID,
        maxResults = maxResults ?? options.maxResults,
        portfolioID = portfolioID ?? options.portfolioID,
        userID = userID ?? options.userID)
    }

  }

}