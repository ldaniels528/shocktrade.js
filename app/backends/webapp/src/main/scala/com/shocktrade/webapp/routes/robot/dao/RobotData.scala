package com.shocktrade.webapp.routes.robot.dao

import scala.scalajs.js

/**
 * Represents an autonomous trading robot
 * @param robotID        the robot's unique identifier
 * @param username       the name of the robot
 * @param contestName    the name of the contest
 * @param userID         the robot's user ID
 * @param contestID      the robot's contest ID
 * @param portfolioID    the robot's portfolio ID
 * @param funds          the robot's investment budget
 * @param strategy       the trading strategy (e.g. "penny-stock")
 * @param lastActivity   the last activity the robot engaged in
 * @param lastActiveTime the last time the robot was active
 * @param isActive       indicates whether the robot is active
 */
class RobotData(val robotID: js.UndefOr[Int],
                val username: js.UndefOr[String],
                val contestName: js.UndefOr[String],
                val userID: js.UndefOr[String],
                val contestID: js.UndefOr[String],
                val portfolioID: js.UndefOr[String],
                val funds: js.UndefOr[Double],
                val strategy: js.UndefOr[String],
                val lastActivity: js.UndefOr[String],
                val lastActiveTime: js.UndefOr[js.Date],
                val isActive: js.UndefOr[Boolean]) extends js.Object