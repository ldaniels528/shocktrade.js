package com.shocktrade.server.tqm.data

import com.shocktrade.javascript.models.contest._
import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js

/**
  * Portfolio Data Model for TQM
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait PortfolioData extends js.Object {
  var _id: js.UndefOr[ObjectID] = js.native
  var contestID: js.UndefOr[String] = js.native
  var playerID: js.UndefOr[String] = js.native

  var status: js.UndefOr[String] = js.native
  var cashAccount: js.UndefOr[CashAccount] = js.native
  var marginAccount: js.UndefOr[MarginAccount] = js.native
  var orders: js.UndefOr[js.Array[Order]] = js.native
  var closedOrders: js.UndefOr[js.Array[ClosedOrder]] = js.native
  var performance: js.UndefOr[js.Array[Performance]] = js.native
  var perks: js.UndefOr[js.Array[String]] = js.native
  var positions: js.UndefOr[js.Array[Position]] = js.native

  // administrative fields
  var lastUpdate: js.UndefOr[Double] = js.native
  var nextUpdate: js.UndefOr[Double] = js.native
  
}
