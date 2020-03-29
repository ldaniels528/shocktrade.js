package com.shocktrade.webapp.routes.account.dao

import scala.scalajs.js

/**
 * Represents an Award Data model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AwardData(val awardID: js.UndefOr[String] = js.undefined,
                val name: js.UndefOr[String] = js.undefined,
                val code: js.UndefOr[String] = js.undefined,
                val icon: js.UndefOr[String] = js.undefined,
                val description: js.UndefOr[String] = js.undefined) extends js.Object