package com.shocktrade.robots

import com.shocktrade.common.models.contest.ContestRanking

import scala.scalajs.js

case class RobotContext(robotName: String,
                        contestID: String,
                        portfolioID: String,
                        userID: String,
                        var previousRankings: js.UndefOr[js.Array[ContestRanking]])
