package com.shocktrade.robots.dao

import com.shocktrade.common.models.contest.PortfolioLike

import scala.scalajs.js

/**
 * Represents an autonomous trading robot's portfolio
 * @param robotID     the robot's unique identifier
 * @param username    the name of the robot
 * @param contestName the name of the contest
 * @param userID      the robot's user ID
 * @param contestID   the robot's contest ID
 * @param portfolioID the robot's portfolio ID
 * @param funds       the robot's investment budget
 * @param totalXP     the total experience gained for this portfolio
 * @param strategy    the trading strategy (e.g. "penny-stock")
 * @param closedTime  the time the portfolio was closed
 * @param isActive    indicates whether the robot is active
 */
class RobotPortfolioData(val robotID: js.UndefOr[Int],
                         val username: js.UndefOr[String],
                         val contestName: js.UndefOr[String],
                         val userID: js.UndefOr[String],
                         val contestID: js.UndefOr[String],
                         val portfolioID: js.UndefOr[String],
                         val funds: js.UndefOr[Double],
                         val totalXP: js.UndefOr[Double],
                         val strategy: js.UndefOr[String],
                         val closedTime: js.UndefOr[js.Date],
                         val isActive: js.UndefOr[Boolean]) extends PortfolioLike