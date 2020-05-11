package com.shocktrade.client.contest.models

import com.shocktrade.common.models.contest.{PortfolioBalance, PortfolioLike, Position}

import scala.scalajs.js

/**
 * Represents a Portfolio graph model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Portfolio(val portfolioID: js.UndefOr[String] = js.undefined,
                val contestID: js.UndefOr[String] = js.undefined,
                val userID: js.UndefOr[String] = js.undefined,
                val username: js.UndefOr[String] = js.undefined,
                val active: js.UndefOr[Boolean] = js.undefined,
                val funds: js.UndefOr[Double] = js.undefined,
                val totalXP: js.UndefOr[Double] = js.undefined,
                val balance: js.UndefOr[PortfolioBalance] = js.undefined,
                val perks: js.UndefOr[js.Array[String]] = js.undefined,
                val orders: js.UndefOr[js.Array[Order]] = js.undefined,
                val positions: js.UndefOr[js.Array[Position]] = js.undefined,
                val joinTime: js.UndefOr[js.Date] = js.undefined,
                val closedTime: js.UndefOr[js.Date] = js.undefined) extends PortfolioLike