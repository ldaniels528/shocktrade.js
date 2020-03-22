package com.shocktrade.webapp.routes.contest.dao

import scala.scalajs.js

/**
 * Represents the Perk Data model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PerkData(val _id: js.UndefOr[String] = js.undefined,
               val name: js.UndefOr[String] = js.undefined,
               val code: js.UndefOr[String] = js.undefined,
               val description: js.UndefOr[String] = js.undefined,
               val cost: js.UndefOr[Double] = js.undefined) extends js.Object