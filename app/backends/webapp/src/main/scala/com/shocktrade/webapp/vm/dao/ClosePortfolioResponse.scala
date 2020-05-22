package com.shocktrade.webapp.vm.dao

import com.shocktrade.common.models.contest.PortfolioEquity

import scala.scalajs.js

/**
 * Close Portfolio Response
 * @param proceeds       the given [[PortfolioEquity]]
 * @param recommendation the given [[AwardsRecommendation]]
 * @param xp             the given XP amount
 */
class ClosePortfolioResponse(val proceeds: js.UndefOr[PortfolioEquity],
                             val recommendation: js.UndefOr[AwardsRecommendation],
                             val xp: js.UndefOr[Double]) extends js.Object