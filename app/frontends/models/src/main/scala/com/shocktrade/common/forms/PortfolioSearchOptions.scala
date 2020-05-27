package com.shocktrade.common.forms

import scala.scalajs.js

/**
 * Portfolio Search Options
 * @param contestID   the given contest ID
 * @param userID      the given user ID
 * @param portfolioID the given portfolio ID
 */
class PortfolioSearchOptions(val contestID: js.UndefOr[String] = js.undefined,
                             val userID: js.UndefOr[String] = js.undefined,
                             val portfolioID: js.UndefOr[String] = js.undefined) extends js.Object

/**
 * Portfolio Search Options
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PortfolioSearchOptions {

  def apply(contestID: js.UndefOr[String] = js.undefined,
            userID: js.UndefOr[String] = js.undefined,
            portfolioID: js.UndefOr[String] = js.undefined): PortfolioSearchOptions = {
    new PortfolioSearchOptions(
      contestID = contestID,
      portfolioID = portfolioID,
      userID = userID
    )
  }

}