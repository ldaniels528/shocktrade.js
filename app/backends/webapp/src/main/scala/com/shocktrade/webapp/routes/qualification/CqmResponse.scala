package com.shocktrade.webapp.routes.qualification

import com.shocktrade.webapp.routes.contest.dao.{OrderData, PositionData}

import scala.scalajs.js

class CqmResponse(val positions: js.UndefOr[js.Array[PositionData]],
                  val updatedOrders: js.UndefOr[js.Array[OrderData]],
                  val closedCount: js.UndefOr[Int],
                  val positionCount: js.UndefOr[Int],
                  val updatedOrderCount: js.UndefOr[Int]) extends js.Object
