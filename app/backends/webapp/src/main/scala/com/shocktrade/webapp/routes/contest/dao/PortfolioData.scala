package com.shocktrade.webapp.routes.contest.dao

import scala.scalajs.js

/**
 * Portfolio Data model for the Qualification Engine
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PortfolioData(var portfolioID: js.UndefOr[String] = js.undefined,
                    var contestID: js.UndefOr[String] = js.undefined,
                    var userID: js.UndefOr[String] = js.undefined,

                    // common fields
                    var funds: js.UndefOr[Double] = js.undefined,
                    var asOfDate: js.UndefOr[js.Date] = js.undefined,
                    var active: js.UndefOr[Boolean] = js.undefined,

                    // administrative fields
                    var lastUpdate: js.UndefOr[js.Date] = js.undefined,
                    var nextUpdate: js.UndefOr[js.Date] = js.undefined,
                    var processingHost: js.UndefOr[String] = js.undefined) extends js.Object
