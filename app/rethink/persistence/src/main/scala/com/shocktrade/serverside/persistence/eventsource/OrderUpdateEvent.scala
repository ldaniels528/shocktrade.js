package com.shocktrade.serverside.persistence.eventsource

import java.util.UUID

import com.shocktrade.serverside.persistence.eventsource.PriceTypes.PriceType

import scala.concurrent.duration._
import scala.scalajs.js

/**
  * Represents an Order Update Event
  * @author lawrence.daniels@gmail.com
  */
class OrderUpdateEvent(val name: js.UndefOr[String] = classOf[OrderUpdateEvent].getSimpleName,
                       val uuid: js.UndefOr[String] = UUID.randomUUID().toString,
                       val userID: js.UndefOr[String],
                       val orderID: js.UndefOr[String],
                       val price: js.UndefOr[Double] = js.undefined,
                       val priceType: js.UndefOr[PriceType] = js.undefined,
                       val quantity: js.UndefOr[Int] = js.undefined,
                       val effectiveTime: js.UndefOr[js.Date] = new js.Date(),
                       val expirationTime: js.UndefOr[js.Date] = new js.Date(js.Date.now() + 3.days.toMillis)) extends OrderEvent