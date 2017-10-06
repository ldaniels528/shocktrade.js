package com.shocktrade.serverside.persistence

import com.shocktrade.eventsource.SourcedEvent

/**
  * eventsource package object
  * @author lawrence.daniels@gmail.com
  */
package object eventsource {

  /**
    * Portfolio Event Enrichment
    * @param event the given [[SourcedEvent event]]
    */
  final implicit class PortfolioEventEnrichment(val event: SourcedEvent) extends AnyVal {

    //////////////////////////////////////////////////////////////////////////
    //  Orders
    //////////////////////////////////////////////////////////////////////////

    @inline
    def isOrder: Boolean = event.name.exists(_.startsWith("Order"))

    @inline
    def isOrderCreation: Boolean = event.name.contains("OrderCreationEvent")

    @inline
    def isOrderClose: Boolean = event.name.contains("OrderCloseEvent")

    @inline
    def isOrderUpdate: Boolean = event.name.contains("OrderUpdateEvent")

    //////////////////////////////////////////////////////////////////////////
    //  Positions
    //////////////////////////////////////////////////////////////////////////

    @inline
    def isPosition: Boolean = event.name.exists(_.startsWith("Position"))

    @inline
    def isPositionCreation: Boolean = event.name.contains("PositionCreationEvent")

    @inline
    def isPositionUpdate: Boolean = event.name.contains("PositionUpdateEvent")

  }

}
