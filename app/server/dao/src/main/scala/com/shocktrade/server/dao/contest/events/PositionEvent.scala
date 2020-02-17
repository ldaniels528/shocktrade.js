package com.shocktrade.server.dao.contest.events

import com.shocktrade.server.dao.events.SourcedEvent

import scala.scalajs.js

/**
  * Represents a generic Position Event
  * @author lawrence.daniels@gmail.com
  */
trait PositionEvent extends OrderEvent {

  def positionID: js.UndefOr[String]

}

/**
  * Position Event Companion
  * @author lawrence.daniels@gmail.com
  */
object PositionEvent {

  /**
    * Position Event Enrichment
    * @param event the given [[SourcedEvent event]]
    */
  final implicit class PositionEventEnrichment(val event: SourcedEvent) extends AnyVal {

    @inline
    def isPosition: Boolean = event.name.exists(_.startsWith("Position"))

    @inline
    def isPositionCreation: Boolean = event.name.contains("PositionCreationEvent")

    @inline
    def isPositionUpdate: Boolean = event.name.contains("PositionUpdateEvent")

  }

}
