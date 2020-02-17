package com.shocktrade.server.dao.contest.events

import java.util.UUID

import scala.scalajs.js

/**
  * Represents the update of a new position
  * @author lawrence.daniels@gmail.com
  */
class PositionUpdateEvent(val name: js.UndefOr[String] = classOf[PositionUpdateEvent].getSimpleName,
                          val uuid: js.UndefOr[String] = UUID.randomUUID().toString,
                          val userID: js.UndefOr[String],
                          val positionID: js.UndefOr[String],
                          val orderID: js.UndefOr[String],
                          val price: js.UndefOr[Double],
                          val quantity: js.UndefOr[Int], // negative = sold, positive = bought
                          val creationTime: js.UndefOr[js.Date] = new js.Date(),
                          val effectiveTime: js.UndefOr[js.Date] = new js.Date()) extends PositionEvent