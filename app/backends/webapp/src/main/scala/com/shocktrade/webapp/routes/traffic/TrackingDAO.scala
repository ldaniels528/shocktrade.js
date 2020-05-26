package com.shocktrade.webapp.routes.traffic

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.dao.DataAccessObjectHelper.getConnectionOptions
import com.shocktrade.server.dao.MySQLDAO
import com.shocktrade.webapp.routes.traffic.TrackingDAO.{Cancelable, JsArraysEnriched}
import com.shocktrade.webapp.vm.dao.EventSourceData
import io.scalajs.nodejs._
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Tracking DAO
 * @param options the given [[MySQLConnectionOptions]]
 * @param ec      the implicit [[ExecutionContext]]
 */
class TrackingDAO(options: MySQLConnectionOptions = getConnectionOptions)(implicit ec: ExecutionContext) extends MySQLDAO(options) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val eventQueue = js.Array[EventSourceData]()
  private val eventSQLCache = js.Dictionary[String]()
  private var eventsWritten: Int = 0
  private val trafficQueue = js.Array[TrafficData]()
  private val trafficSQLCache = js.Dictionary[String]()

  /**
   * Continuously persists events to the storage layer
   * @param frequency the persistence [[FiniteDuration frequency]]
   * @return a [[Cancelable]]
   */
  def continuouslySaveEvents(frequency: FiniteDuration): Cancelable = {
    val cancelable = new Cancelable()

    def recurse(): Unit = {
      if (cancelable.isAlive) {
        eventQueue match {
          case queue if queue.isEmpty & eventsWritten >= 10 =>
            eventsWritten = 0
            indexEventSource() onComplete (_ => setImmediate(() => recurse()))
          case queue if queue.isEmpty =>
            setTimeout(() => recurse(), frequency)
          case queue =>
            // remove everything from the queue & count these events
            val eventList = queue.transferItems()
            eventsWritten += eventList.size

            // insert the events into the table
            val startTime = js.Date.now()
            lazy val elapsedTime = js.Date.now() - startTime
            val outcome = insertEvents(eventList)
            outcome onComplete (_ => setImmediate(() => recurse()))
            outcome onComplete {
              case Success(count) =>
                logger.info(s"Wrote $count event records in $elapsedTime msec")
              case Failure(e) =>
                logger.error(s"Failed to write events after $elapsedTime msec: ${e.getMessage}")
                e.printStackTrace()
            }
        }
      }
    }

    recurse()
    cancelable
  }

  /**
   * Continuously persists web traffic to the storage layer
   * @param frequency the persistence [[FiniteDuration frequency]]
   * @return a [[Cancelable]]
   */
  def continuouslySaveTraffic(frequency: FiniteDuration): Cancelable = {
    val cancelable = new Cancelable()

    def recurse(): Unit = {
      if (cancelable.isAlive) {
        trafficQueue match {
          case queue if queue.isEmpty => setTimeout(() => recurse(), frequency)
          case queue =>
            // remove everything from the queue
            val trafficList = queue.transferItems()

            // insert the traffic into the table
            val startTime = js.Date.now()
            lazy val elapsedTime = js.Date.now() - startTime
            val outcome = insertTraffic(trafficList)
            outcome onComplete (_ => setImmediate(() => recurse()))
            outcome onComplete {
              case Success(count) =>
                logger.info(s"Wrote $count web traffic records in $elapsedTime msec")
              case Failure(e) =>
                logger.error(s"Failed to write ${trafficList.size} web traffic records after $elapsedTime msec: ${e.getMessage}")
                e.printStackTrace()
            }
        }
      }
    }

    recurse()
    cancelable
  }

  def trackEvent(event: EventSourceData): Int = eventQueue.push(event)

  def trackRequest(traffic: TrafficData): Int = trafficQueue.push(traffic)

  private def indexEventSource(): Future[Int] = {
    val startTime = js.Date.now()
    val outcome = updateEventsWithUserAndContestID()
    outcome onComplete {
      case Success(count) =>
        val elapsedTime = js.Date.now() - startTime
        logger.info(s"Indexed $count events in $elapsedTime msec")
      case Failure(e) =>
        val elapsedTime = js.Date.now() - startTime
        logger.info(s"Failed to update events after $elapsedTime msec: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome
  }

  private def insertEvents(events: Seq[EventSourceData]): Future[Int] = {
    conn.executeFuture(eventSQLCache.getOrElseUpdate(events.length.toString,
      s"""|INSERT INTO eventsource (
          |  command, type, contestID, portfolioID, userID, orderID, positionID, orderType, priceType,
          |  negotiatedPrice, quantity, symbol, exchange, xp, failed, response, responseTimeMillis
          |) VALUES ${events map (_ => "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)") mkString ","}
          |""".stripMargin),
      events flatMap { event =>
        import event._
        Seq(command, `type`, contestID, portfolioID, userID, orderID, positionID, orderType, priceType,
          negotiatedPrice, quantity, symbol, exchange, xp, failed, response, responseTimeMillis).map(_.orNull)
      }).map(_.affectedRows)
  }

  private def insertTraffic(traffic: Seq[TrafficData]): Future[Int] = {
    conn.executeFuture(trafficSQLCache.getOrElseUpdate(traffic.length.toString,
      s"""|INSERT INTO traffic (method, path, query, statusCode, statusMessage, responseTimeMillis, requestTime, creationTime)
          |VALUES ${traffic map (_ => "(?, ?, ?, ?, ?, ?, ?, ?)") mkString ","}
          |""".stripMargin),
      traffic flatMap { item =>
        import item._
        Seq(method, path, query, statusCode, statusMessage, responseTimeMillis, requestTime, creationTime).map(_.orNull)
      }).map(_.affectedRows)
  }

  private def updateEventsWithUserAndContestID(): Future[Int] = {
    for {
      // is contestID or userID null?
      w0 <- conn.executeFuture(
        """|UPDATE eventsource E
           |INNER JOIN portfolios P ON P.portfolioID = E.portfolioID
           |SET E.contestID = P.contestID, E.userID = P.userID
           |WHERE E.portfolioID IS NOT NULL
           |AND (E.contestID IS NULL OR E.userID IS NULL)
           |""".stripMargin).map(_.affectedRows)

      // is portfolioID null?
      w1 <- conn.executeFuture(
        """|UPDATE eventsource E
           |INNER JOIN portfolios P on P.contestID = E.contestID AND P.userID = E.userID
           |SET E.portfolioID = P.contestID
           |WHERE E.portfolioID IS NULL
           |AND (E.contestID IS NOT NULL AND E.userID IS NOT NULL)
           |""".stripMargin).map(_.affectedRows)
    } yield w0 + w1
  }

}

/**
 * Tracking DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object TrackingDAO {

  /**
   * Cancelable promise
   * @param isAlive indicates whether the continuous promise is active
   */
  class Cancelable(var isAlive: Boolean = true) extends js.Object

  /**
   * Cancelable Enriched
   * @param cancelable the given [[Cancelable]]
   */
  final implicit class CancelableEnriched(val cancelable: Cancelable) extends AnyVal {
    @inline def cancel(): Unit = cancelable.isAlive = false
  }

  /**
   * js.Array Enriched
   * @param queue the given [[js.Array]]
   */
  final implicit class JsArraysEnriched[T](val queue: js.Array[T]) extends AnyVal {

    @inline
    def transferItems(): List[T] = {
      val items = queue.toList
      queue.remove(0, items.size)
      items
    }
  }

}