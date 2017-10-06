package com.shocktrade.serverside.persistence.dao

import com.shocktrade.serverside.persistence.eventsource.OrderTypes.OrderType
import com.shocktrade.serverside.persistence.eventsource.PriceTypes.PriceType
import com.shocktrade.serverside.persistence.eventsource.{OrderCloseEvent, OrderCreationEvent, OrderUpdateEvent}

import scala.scalajs.js

/**
  * Represents an Order data object
  * @author lawrence.daniels@gmail.com
  */
class OrderData(val orderID: String,
                val userID: String,
                val symbol: js.UndefOr[String],
                val exchange: js.UndefOr[String],
                val orderType: js.UndefOr[OrderType],
                val price: js.UndefOr[Double],
                val priceType: js.UndefOr[PriceType],
                val quantity: js.UndefOr[Int],
                val effectiveTime: js.Date,
                val expirationTime: js.UndefOr[js.Date],
                val message: js.UndefOr[String] = js.undefined) extends js.Object

/**
  * Order Data Companion
  * @author lawrence.daniels@gmail.com
  */
object OrderData {

  /**
    * Order Close Event Enrichment
    * @param event the given [[OrderCloseEvent event]]
    */
  final implicit class OrderCloseEventEnrichment(val event: OrderCloseEvent) extends AnyVal {

    @inline
    def toDataModel: Option[OrderData] = {
      (for {
        orderID <- event.orderID
        userID <- event.userID
        orderType = js.undefined
        price = js.undefined
        priceType = js.undefined
        quantity = js.undefined
        effectiveTime <- event.effectiveTime
        expirationTime = js.undefined
      } yield new OrderData(orderID, userID, symbol = js.undefined, exchange = js.undefined, orderType, price, priceType, quantity, effectiveTime, expirationTime)).toOption
    }
  }

  /**
    * Order Creation Event Enrichment
    * @param event the given [[OrderCreationEvent event]]
    */
  final implicit class OrderCreationEventEnrichment(val event: OrderCreationEvent) extends AnyVal {

    @inline
    def toDataModel: Option[OrderData] = {
      (for {
        orderID <- event.orderID
        userID <- event.userID
        symbol <- event.symbol
        exchange <- event.exchange
        orderType <- event.orderType
        price = event.price
        priceType <- event.priceType
        quantity <- event.quantity
        effectiveTime <- event.effectiveTime
        expirationTime <- event.expirationTime
      } yield new OrderData(orderID, userID, symbol, exchange, orderType, price, priceType, quantity, effectiveTime, expirationTime)).toOption
    }
  }

  /**
    * Order Update Event Enrichment
    * @param event the given [[OrderUpdateEvent event]]
    */
  final implicit class OrderUpdateEventEnrichment(val event: OrderUpdateEvent) extends AnyVal {

    @inline
    def toDataModel: Option[OrderData] = {
      (for {
        orderID <- event.orderID
        userID <- event.userID
        orderType = js.undefined
        price = event.price
        priceType <- event.priceType
        quantity <- event.quantity
        effectiveTime <- event.effectiveTime
        expirationTime <- event.expirationTime
      } yield new OrderData(orderID, userID, symbol = js.undefined, exchange = js.undefined, orderType, price, priceType, quantity, effectiveTime, expirationTime)).toOption
    }
  }

}