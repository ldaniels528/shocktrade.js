package com.shocktrade.eventsource

import java.util.UUID

import com.shocktrade.eventsource.EventSource.EventSubscription
import io.scalajs.nodejs.setImmediate

import scala.scalajs.js

/**
  * Represents an Event Source
  * @author lawrence.daniels@gmail.com
  */
trait EventSource {
  private val subscriptions = new js.Array[EventSubscription]()

  /**
    * Adds a new event
    * @param event the given [[SourcedEvent event]]
    */
  def +=(event: SourcedEvent): Unit = add(event)

  /**
    * Adds a new event
    * @param event the given [[SourcedEvent event]]
    */
  def add(event: SourcedEvent): Unit = {
    // persist the event to disk
    persistEvent(event)

    // fire off the event to subscribers
    setImmediate(() => for (subscription <- subscriptions) subscription(event))
    ()
  }

  /**
    * Registers a new subscriber for event updates
    * @param subscription the given subscriber update function
    */
  def register(subscription: EventSubscription): Unit = subscriptions.append(subscription)

  /**
    * Replays events from the source
    * @param handler the given event handler
    */
  def replay(handler: SourcedEvent => Unit): Unit

  /**
    * Overriden by implementing classes to persist the event
    * @param event the given [[SourcedEvent event]]
    * @return true, if the event was successfully persisted
    */
  protected def persistEvent(event: SourcedEvent): Boolean

}

/**
  * Event Source Companion
  * @author lawrence.daniels@gmail.com
  */
object EventSource {

  /**
    * Represents an event subscription
    */
  type EventSubscription = SourcedEvent => Unit

  /**
    * Retrieves a file-oriented file event source
    * @param outputPath the given output path
    * @return the [[FileEventSource file-event source]]
    */
  def fromFile(outputPath: String): EventSource = new FileEventSource(outputPath)

  /**
    * Generates a unique ID
    * @return a unique ID
    */
  def generateUID: String = UUID.randomUUID().toString

}