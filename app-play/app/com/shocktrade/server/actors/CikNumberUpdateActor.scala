package com.shocktrade.server.actors

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.controllers.QuotesController._
import com.shocktrade.server.actors.CikNumberUpdateActor._
import com.shocktrade.services.CikCompanySearchService
import com.shocktrade.services.CikCompanySearchService.CikInfo
import play.libs.Akka
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, BSONNull}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
 * CIK Number Update Actor
 * @author lawrence.daniels@gmail.com
 */
class CikNumberUpdateActor extends Actor with ActorLogging {
  implicit val ec = context.dispatcher
  private lazy val mc = db.collection[BSONCollection]("Stocks")
  private val counter = new AtomicInteger()

  override def receive = {
    case missingCik: MissingCik =>
      processCikUpdate(missingCik)

    case UpdateMissingCikNumbers =>
      counter.set(0)
      val mySender = sender()
      findMissingCikSymbols() foreach { symbols =>
        symbols foreach (CikNumberUpdateActor ! _)
        mySender ! symbols.length
      }

    case message =>
      log.error(s"Unhandled message: $message (${Option(message).map(_.getClass.getName).orNull}})")
      unhandled(message)
  }

  private def findMissingCikSymbols() = {
    log.info(s"Searching for records missing CIK information...")

    // query the missing symbols
    // db.Stocks.count({"active":true, "assetType":"Common Stock", "name":{"$ne":null}, "cikNumber":{"$exists":false}});
    // db.Stocks.find({"active":true, "assetType":"Common Stock", "name":{"$ne":null}, "cikNumber":{"$exists":false}});
    val missingCiks = mc.find(
      BS("active" -> true, "assetType" -> "Common Stock", "name" -> BS("$ne" -> BSONNull), "cikNumber" -> BS("$exists" -> false)),
      BS("symbol" -> 1, "name" -> 1))
      .cursor[MissingCik]()
      .collect[Seq]()

    missingCiks.map { records =>
      log.info(s"Retrieving ${records.length} record(s)")
    }
    missingCiks
  }

  private def lookupCikData(name: String) = {
    Try(CikCompanySearchService.search(name)) match {
      case Success(cikInfo) => cikInfo
      case Failure(e) =>
        log.error(e, s"Error retrieve CIK information for '$name'")
        Nil
    }
  }

  private def processCikUpdate(missingCik: MissingCik) {
    val results = lookupCikData(missingCik.name)
    results.headOption foreach { cikInfo =>
      log.warning(s"CIK ${cikInfo.cikNumber} found for '${missingCik.name}' (${missingCik.symbol})")
      persistCik(missingCik.symbol, missingCik.name, cikInfo)
    }
  }

  /**
   * Writes the updated CIK information to the data store
   */
  private def persistCik(symbol: String, name: String, cik: CikInfo) {
    import cik._

    mc.update(
      BS("symbol" -> symbol),
      if (name.length < cikName.length)
        BS("$set" -> BS("name" -> cikName, "cikNumber" -> cikNumber))
      else
        BS("$set" -> BS("cikNumber" -> cikNumber)),
      upsert = false, multi = false)

    // log the statistics
    if (counter.incrementAndGet() % 10 == 0) {
      log.info(s"Processed ${counter.get} CIKs")
    }
  }

}

/**
 * CIK Number Update Actor
 * @author lawrence.daniels@gmail.com
 */
object CikNumberUpdateActor {
  private val myActor = Akka.system.actorOf(Props[CikNumberUpdateActor].withRouter(RoundRobinPool(nrOfInstances = 5)), name = "CikNumberUpdate")

  def !(message: Any) = myActor ! message

  def ?(message: Any)(implicit ec: ExecutionContext, timeout: Timeout) = myActor ? message

  case object UpdateMissingCikNumbers

}
