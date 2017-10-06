package com.shocktrade.serverside.persistence

import com.shocktrade.eventsource.{FileEventSource, SourcedEvent}
import com.shocktrade.serverside.persistence.dao.OrderData._
import com.shocktrade.serverside.persistence.dao.{OrderDAO, PositionDAO}
import com.shocktrade.serverside.persistence.eventsource._
import io.scalajs.JSON
import io.scalajs.nodejs.{console, setInterval}
import io.scalajs.util.DurationHelper._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * Qualification Server
  * @author lawrence.daniels@gmail.com
  */
object QualificationServer {
  private val version = "0.10"

  // define the event source
  private val eventSource = new FileEventSource("./eventSource.json")

  // create the DAOs
  private val orderDAO = OrderDAO()
  private val positionDAO = PositionDAO()

  /**
    * Application entry-point
    */
  @JSExport
  def main(args: Array[String]): Unit = {
    console.log(s"Qualification Server v$version")

    // register our DAOs for updates
    eventSource.register(handleEvent)

    // process orders once per minute
    setInterval(() => processOrders(), 15.seconds)
  }

  /**
    * Handles all events
    * @param event the given [[SourcedEvent event]]
    */
  def handleEvent(event: SourcedEvent): Unit = {
    val outcome = event match {
      case evt if evt.isOrderClose => evt.asInstanceOf[OrderCloseEvent].toDataModel.map(orderDAO.closeOrder).getOrElse(fail)
      case evt if evt.isOrderCreation => evt.asInstanceOf[OrderCreationEvent].toDataModel.map(orderDAO.createOrder).getOrElse(fail)
      case evt if evt.isOrderUpdate => evt.asInstanceOf[OrderUpdateEvent].toDataModel.map(orderDAO.updateOrder).getOrElse(fail)
      case evt if evt.isPositionCreation => positionDAO.createPosition(evt.asInstanceOf[PositionCreationEvent])
      case evt if evt.isPositionUpdate => positionDAO.updatePosition(evt.asInstanceOf[PositionUpdateEvent])
      case evt => Future.failed(js.JavaScriptException(s"Unhandled event: ${JSON.stringify(evt)}"))
    }
    outcome onComplete {
      case Success(_) =>
        console.log(s"Processed: ${JSON.stringify(event)}")
      case Failure(e) =>
        console.error(e.getMessage, event)
        e.printStackTrace()
    }
  }

  def processOrders(): Unit = {
    val effectiveTime = new js.Date()
    console.log(s"[$effectiveTime] Attempting to create positions...")
    orderDAO.processOrders(effectiveTime) onComplete {
      case Success(count) =>
        if (count > 0) console.log(s"[$effectiveTime] $count claim(s) created.")
        else console.log(s"[$effectiveTime] No claims created.")
      case Failure(e) =>
        console.error(s"Order processing failed: ${e.getMessage}")
    }
  }

  private def fail = Future.failed(js.JavaScriptException("Failed to convert event to data object"))

}
