package com.shocktrade.robots

import com.shocktrade.robots.dao.RobotPortfolioData

case class RobotState(robotName: String,
                      contestID: String,
                      userID: String,
                      portfolio: RobotPortfolioData,
                      var prevRankNum_? : Option[Int] = None)
