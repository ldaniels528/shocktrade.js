package com.shocktrade.common.dao.contest

import com.shocktrade.common.models.contest._
import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Portfolio Data model for the Qualification Engine
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class PortfolioData(var _id: js.UndefOr[ObjectID] = js.undefined,
                    var contestID: js.UndefOr[String] = js.undefined,
                    var contestName: js.UndefOr[String] = js.undefined,

                    // common fields
                    var playerID: js.UndefOr[String] = js.undefined,
                    var active: js.UndefOr[Boolean] = true,
                    var cashAccount: js.UndefOr[CashAccount] = js.undefined,
                    var marginAccount: js.UndefOr[MarginAccount] = js.undefined,
                    var orders: js.UndefOr[js.Array[OrderData]] = js.undefined,
                    var closedOrders: js.UndefOr[js.Array[OrderData]] = js.undefined,
                    var performance: js.UndefOr[js.Array[PerformanceLike]] = js.undefined,
                    var perks: js.UndefOr[js.Array[String]] = js.undefined,
                    var positions: js.UndefOr[js.Array[PositionData]] = js.undefined,

                    // administrative fields
                    var lastUpdate: js.UndefOr[js.Date] = js.undefined,
                    var nextUpdate: js.UndefOr[js.Date] = js.undefined,
                    var processingHost: js.UndefOr[String] = js.undefined) extends PortfolioLike

/**
  * Portfolio Data Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PortfolioData {

  /**
    * Portfolio Enrichment
    * @param portfolio the given [[PortfolioData portfolio]]
    */
  implicit class PortfolioEnrichment(val portfolio: PortfolioData) extends AnyVal {

    @inline
    def findEligibleOrders(asOfTime: js.Date = new js.Date()) = for {
      orders <- portfolio.orders.toOption.map(_.toSeq).toList
      order <- orders.filter(!_.isExpired(asOfTime))
    } yield order

  }

}