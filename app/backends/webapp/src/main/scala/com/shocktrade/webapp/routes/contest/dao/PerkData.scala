package com.shocktrade.webapp.routes.contest.dao

import scala.scalajs.js

/**
 * Represents the Perk data
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PerkData(val perkID: js.UndefOr[String] = js.undefined,
               val portfolioID: js.UndefOr[String] = js.undefined,
               val perkCode: js.UndefOr[String] = js.undefined,
               val purchasedTime: js.UndefOr[js.Date] = js.undefined) extends js.Object