package com.shocktrade.serverside.persistence.eventsource

import java.util.UUID

import scala.scalajs.js

/**
  * Represents an Order Close Event; this event could occur via user-initiated cancellation or
  * fulfillment of the order by the Qualification system
  * @author lawrence.daniels@gmail.com
  */
class OrderCloseEvent(val name: js.UndefOr[String] = classOf[OrderCloseEvent].getSimpleName,
                      val uuid: js.UndefOr[String] = UUID.randomUUID().toString,
                      val userID: js.UndefOr[String],
                      val orderID: js.UndefOr[String],
                      val userInitiated: Boolean,
                      val fulfilled: Boolean,
                      val effectiveTime: js.UndefOr[js.Date] = new js.Date()) extends OrderEvent