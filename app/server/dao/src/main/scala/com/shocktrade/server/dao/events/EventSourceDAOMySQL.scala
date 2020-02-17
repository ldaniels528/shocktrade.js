package com.shocktrade.server.dao.events

import com.shocktrade.server.dao.contest.events._
import com.shocktrade.server.dao.events.EventSourceDAOMySQL.EventData
import io.scalajs.JSON
import io.scalajs.npm.moment.Moment
import io.scalajs.npm.mysql.{ConnectionOptions, MySQL}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

/**
  * Event Source DAO (MySQL implementation)
  * @author lawrence.daniels@gmail.com
  */
class EventSourceDAOMySQL(options: ConnectionOptions)(implicit ec: ExecutionContext) extends EventSourceDAO {
  private val datePattern = "YYYY-MM-DD HH:mm:ss"
  private val conn = MySQL.createConnection(options)

  /**
    * Writes the given event to the event store
    * @param event the given [[SourcedEvent event]]
    * @return the promise of the event persistence outcome
    */
  override def create(event: SourcedEvent): Future[Boolean] = {
    val creationTime = Moment(new js.Date()).format(datePattern)
    val eventType = event.getClass.getSimpleName
    conn.executeFuture(
      s"""|INSERT INTO events (eventID, eventType, eventJson, creationTime)
          |VALUES ('${event.uuid}', '$eventType', '${JSON.stringify(event)}', '$creationTime')
          |""".stripMargin
    ) map (_.affectedRows > 0)
  }

  /**
    * Retrieves all events occurring between the begin time and end time
    * @param beginTime the begin time
    * @param endTime   the end time
    * @return the promise of the collection of [[SourcedEvent event]]
    */
  override def fetch(beginTime: js.UndefOr[js.Date] = js.undefined, endTime: js.UndefOr[js.Date] = js.undefined): Future[Seq[SourcedEvent]] = {
    val t0 = beginTime.getOrElse(new js.Date())
    val t1 = endTime.getOrElse(new js.Date())
    val time0 = Moment(t0).format(datePattern)
    val time1 = Moment(t1).format(datePattern)
    conn.queryFuture[EventData](
      s"""|SELECT eventID, eventType, eventJson, creationTime
          |FROM events
          |WHERE creationTime BETWEEN '$time0' AND '$time1'
          |""".stripMargin
    ) map { case (rows, _) => rows.flatMap(_.toModel) }
  }

  /**
    * Retrieves an event by its UUID
    * @param eventID the event UUID
    * @return the promise of the option of the [[SourcedEvent event]]
    */
  override def fetchOne(eventID: String): Future[Option[SourcedEvent]] = {
    conn.queryFuture[EventData](
      s"""|SELECT eventID, eventType, eventJson, creationTime
          |FROM events
          |WHERE eventID = '$eventID'
          |""".stripMargin
    ) map { case (rows, _) => rows.flatMap(_.toModel).headOption }
  }

}

/**
  * Event Source DAO MySQL Singleton
  * @author lawrence.daniels@gmail.com
  */
object EventSourceDAOMySQL {

  /**
    * Represents a persistent event
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait EventData extends js.Object {

    def eventID: js.UndefOr[String]

    def eventType: js.UndefOr[String]

    def eventJson: js.UndefOr[String]

    def creationTime: js.UndefOr[js.Date]
  }

  /**
    * Event Data Conversion
    * @param data the given [[EventData]]
    */
  final implicit class EventDataConversion(val data: EventData) extends AnyVal {

    def toModel: Option[SourcedEvent] = {
      (for {
        eventType <- data.eventType
        eventJson <- data.eventJson
      } yield eventType match {
        case "OrderCloseEvent" => JSON.parseAs[OrderCloseEvent](eventJson)
        case "OrderCreationEvent" => JSON.parseAs[OrderCreationEvent](eventJson)
        case "OrderUpdateEvent" => JSON.parseAs[OrderUpdateEvent](eventJson)
        case "PositionCreationEvent" => JSON.parseAs[PositionCreationEvent](eventJson)
        case "PositionUpdateEvent" => JSON.parseAs[PositionUpdateEvent](eventJson)
        case unknown => throw js.JavaScriptException(s"Unrecognized event type '$unknown' (${JSON.stringify(data)})")
      }).toOption
    }

  }

}
