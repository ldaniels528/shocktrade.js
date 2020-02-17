package com.shocktrade.server.dao.events

import scala.concurrent.Future

/**
  * Base class for all Event Consumers
  * @author lawrence.daniels@gmail.com
  */
trait EventConsumer[T <: SourcedEvent] {

  /**
    * Attempts to consume the given event
    * @param event the given [[SourcedEvent event]]
    * @return the option of a future representing the consumption of the event
    */
  def consume(event: T): Option[Future[Boolean]]

}
