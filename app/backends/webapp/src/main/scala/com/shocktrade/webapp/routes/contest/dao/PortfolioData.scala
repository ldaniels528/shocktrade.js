package com.shocktrade.webapp.routes.contest.dao

import java.util.UUID

import com.shocktrade.common.models.contest.PortfolioLike

import scala.scalajs.js

/**
 * Portfolio Data model for the Qualification Engine
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PortfolioData(val portfolioID: js.UndefOr[String] = UUID.randomUUID().toString,
                    val contestID: js.UndefOr[String] = js.undefined,
                    val userID: js.UndefOr[String] = js.undefined,
                    val funds: js.UndefOr[Double] = js.undefined,
                    val totalXP: js.UndefOr[Double],
                    val joinTime: js.UndefOr[js.Date] = js.undefined,
                    val closedTime: js.UndefOr[js.Date] = js.undefined) extends PortfolioLike
