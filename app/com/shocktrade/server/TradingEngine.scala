package com.shocktrade.server

import java.util.Date

import akka.actor.Props
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.controllers.Application._
import com.shocktrade.models.contest.{Contest, ContestStatus}
import com.shocktrade.server.TradingActor.ProcessOrders
import com.shocktrade.util.BSONHelper._
import com.shocktrade.util.DateUtil._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.iteratee.Iteratee
import play.libs.Akka
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONArray, BSONDocument => BS}

import scala.concurrent.duration._
import scala.language.implicitConversions

/**
 * Trading Engine
 * @author lawrence.daniels@gmail.com
 */
object TradingEngine {
  private lazy val system = Akka.system

  import system.dispatcher

  private lazy val tradingActor = system.actorOf(Props[TradingActor].withRouter(RoundRobinPool(nrOfInstances = 10)), name = "TradingActor")
  private lazy val mc = db.collection[BSONCollection]("Contests")
  private implicit val timeout: Timeout = 30.second
  private var lastEffectiveDate: DateTime = getTradeStartTime

  def init() {
    // process orders once every 5 minutes
    system.scheduler.schedule(5.seconds, 5.minutes, () => processOrders())
  }

  def processOrders() {
    try {
      val currentTime = new Date()

      Logger.info(s"Processing order fulfillment [as of $lastEffectiveDate]...")
      getActiveContests(lastEffectiveDate)(Iteratee.foreach { contest =>
        tradingActor ! ProcessOrders(contest, lastEffectiveDate)
      })

      // update the effective date
      lastEffectiveDate = currentTime;

    } catch {
      case e: Exception =>
        Logger.error("Error processing orders", e)
    }
  }

  /**
   * Queries all active contests that haven't been update in 5 minutes
   * @param asOfDate the last time an update was performed
   * @return a [[play.api.libs.iteratee.Enumerator]] of [[Contest]] instances
   */
  private def getActiveContests(asOfDate: DateTime) = {
    /*
      db.Contests.count({
          "status": "ACTIVE",
          "$or" : [ { processedTime : { "$lte" : new Date() } }, { processedTime : { "$exists" : false } } ],
          "$or" : [ { expirationTime : { "$lte" : new Date() } }, { expirationTime : { "$exists" : false } } ]
      })
     */
    mc.find(BS(
      "status" -> ContestStatus.ACTIVE,
      "$or" -> BSONArray(Seq(BS("processedTime" -> BS("$lte" -> asOfDate.minusMinutes(5))), BS("processedTime" -> BS("$exists" -> false)))),
      "$or" -> BSONArray(Seq(BS("expirationTime" -> BS("$gte" -> asOfDate)), BS("expirationTime" -> BS("$exists" -> false))))
    )).cursor[Contest]
      .enumerate()
  }

  /**
   * Java 8-style anonymous function as a Runnable
   */
  implicit def function0ToRunnable(f: () => Unit): Runnable = new Runnable {
    override def run(): Unit = f()
  }

}
