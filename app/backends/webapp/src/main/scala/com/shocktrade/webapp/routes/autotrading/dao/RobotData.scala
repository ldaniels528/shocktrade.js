package com.shocktrade.webapp.routes.autotrading.dao

import scala.scalajs.js

/**
 * Represents an autonomous trading robot
 * @param robotID     the robot's unique identifier
 * @param username    the name of the robot
 * @param userID      the robot's user ID
 * @param contestID   the robot's contest ID
 * @param portfolioID the robot's portfolio ID
 * @param funds       the robot's investment budget
 * @param strategy    the trading strategy (e.g. "penny-stock")
 */
class RobotData(val robotID: js.UndefOr[Int],
                val username: js.UndefOr[String],
                val userID: js.UndefOr[String],
                val contestID: js.UndefOr[String],
                val portfolioID: js.UndefOr[String],
                val funds: js.UndefOr[Double],
                val strategy: js.UndefOr[String]) extends js.Object