package com.shocktrade.webapp.vm.dao

import com.shocktrade.webapp.vm.dao.VirtualMachineDAOMySQL.PortfolioEquity

import scala.scalajs.js

class ClosePortfolioResponse(val proceeds: js.UndefOr[PortfolioEquity],
                             val recommendation: js.UndefOr[AwardsRecommendation],
                             val xp: js.UndefOr[Int]) extends js.Object