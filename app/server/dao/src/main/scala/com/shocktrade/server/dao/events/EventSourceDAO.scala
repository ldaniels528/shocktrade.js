package com.shocktrade.server.dao.events

import io.scalajs.npm.mysql.ConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Event Source DAO
  * @author lawrence.daniels@gmail.com
  */
trait EventSourceDAO {

  /**
    * Writes the given event to the event store
    * @param event the given [[SourcedEvent event]]
    * @return the option of a future representing the consumption of the event
    */
  def create(event: SourcedEvent): Future[Boolean]

  /**
    * Retrieves all events occurring between the begin time and end time
    * @param beginTime the begin time
    * @param endTime   the end time
    * @return the promise of the collection of [[SourcedEvent]]
    */
  def fetch(beginTime: js.UndefOr[js.Date] = js.undefined, endTime: js.UndefOr[js.Date] = js.undefined): Future[Seq[SourcedEvent]]

  /**
    * Retrieves all events occurring between the begin time and end time
    * @param eventID the event UUID
    * @return the promise of the option of a [[SourcedEvent]]
    */
  def fetchOne(eventID: String): Future[Option[SourcedEvent]]

}

/**
  * Event Source DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object EventSourceDAO {

  def apply(options: ConnectionOptions)(implicit ec: ExecutionContext): EventSourceDAO = new EventSourceDAOMySQL(options)

}
