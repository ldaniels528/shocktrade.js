package com.shocktrade.server.dao.contest.events

import java.util.UUID

import com.shocktrade.server.dao.contest.OrderTypes.OrderType
import com.shocktrade.server.dao.contest.PriceTypes.PriceType

import scala.concurrent.duration._
import scala.scalajs.js

/**
  * Represents an Order Creation Event
  * @author lawrence.daniels@gmail.com
  */
class OrderCreationEvent(val name: js.UndefOr[String] = classOf[OrderCreationEvent].getSimpleName,
                         val uuid: js.UndefOr[String] = UUID.randomUUID().toString,
                         val userID: js.UndefOr[String],
                         val orderID: js.UndefOr[String],
                         val symbol: js.UndefOr[String],
                         val exchange: js.UndefOr[String],
                         val orderType: js.UndefOr[OrderType],
                         val price: js.UndefOr[Double],
                         val priceType: js.UndefOr[PriceType],
                         val quantity: js.UndefOr[Int],
                         val creationTime: js.UndefOr[js.Date] = new js.Date(),
                         val effectiveTime: js.UndefOr[js.Date] = new js.Date(),
                         val expirationTime: js.UndefOr[js.Date] = new js.Date(js.Date.now() + 3.days.toMillis)) extends OrderEvent
