package com.shocktrade.common.dao.contest

import com.shocktrade.common.models.contest._
import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js

/**
  * Portfolio Data model for the Qualification Engine
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait PortfolioData extends PortfolioLike {
  var _id: js.UndefOr[ObjectID] = js.native
  var contestID: js.UndefOr[ObjectID] = js.native

  // common fields
  var playerID: js.UndefOr[String] = js.native
  var cashAccount: js.UndefOr[CashAccount] = js.native
  var marginAccount: js.UndefOr[MarginAccount] = js.native
  var orders: js.UndefOr[js.Array[OrderData]] = js.native
  var closedOrders: js.UndefOr[js.Array[OrderData]] = js.native
  var performance: js.UndefOr[js.Array[PerformanceLike]] = js.native
  var perks: js.UndefOr[js.Array[String]] = js.native
  var positions: js.UndefOr[js.Array[PositionData]] = js.native

  // administrative fields
  var lastUpdate: js.UndefOr[js.Date] = js.native
  var nextUpdate: js.UndefOr[js.Date] = js.native
  var processingHost: js.UndefOr[String] = js.native

}

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