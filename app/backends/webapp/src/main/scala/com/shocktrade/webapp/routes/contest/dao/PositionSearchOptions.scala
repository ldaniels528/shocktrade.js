package com.shocktrade.webapp.routes.contest.dao

import scala.scalajs.js

/**
 * Position Search Options
 * @param contestID   the given contest ID
 * @param userID      the given user ID
 * @param portfolioID the given portfolio ID
 * @param positionID  the given position ID
 */
class PositionSearchOptions(val contestID: js.UndefOr[String] = js.undefined,
                            val userID: js.UndefOr[String] = js.undefined,
                            val portfolioID: js.UndefOr[String] = js.undefined,
                            val positionID: js.UndefOr[String] = js.undefined) extends js.Object

/**
 * Position Search Options
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PositionSearchOptions {

  def apply(contestID: js.UndefOr[String] = js.undefined,
            userID: js.UndefOr[String] = js.undefined,
            portfolioID: js.UndefOr[String] = js.undefined,
            positionID: js.UndefOr[String] = js.undefined): PositionSearchOptions = {
    new PositionSearchOptions(
      contestID = contestID,
      portfolioID = portfolioID,
      positionID = positionID,
      userID = userID
    )
  }

}