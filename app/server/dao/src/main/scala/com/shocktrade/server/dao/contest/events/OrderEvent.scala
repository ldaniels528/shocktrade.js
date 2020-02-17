package com.shocktrade.server.dao.contest.events

import com.shocktrade.server.dao.events.SourcedEvent

import scala.scalajs.js

/**
  * Represents a generic Order Event
  * @author lawrence.daniels@gmail.com
  */
trait OrderEvent extends SourcedEvent {

  def orderID: js.UndefOr[String]

  def effectiveTime: js.UndefOr[js.Date]

}

/**
  * Order Event Companion
  * @author lawrence.daniels@gmail.com
  */
object OrderEvent {

  /**
    * Order Event Enrichment
    * @param event the given [[SourcedEvent event]]
    */
  final implicit class OrderEventEnrichment(val event: SourcedEvent) extends AnyVal {

    @inline
    def isOrder: Boolean = event.name.exists(_.startsWith("Order"))

    @inline
    def isOrderCreation: Boolean = event.name.contains("OrderCreationEvent")

    @inline
    def isOrderClose: Boolean = event.name.contains("OrderCloseEvent")

    @inline
    def isOrderUpdate: Boolean = event.name.contains("OrderUpdateEvent")

  }

}
