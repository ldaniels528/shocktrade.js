package com.shocktrade.processors.actors

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging}
import com.shocktrade.dao.SecuritiesUpdateDAO
import com.shocktrade.processors.actors.CikNumberUpdateActor._
import com.shocktrade.services.CikCompanySearchService
import com.shocktrade.services.CikCompanySearchService.CikInfo
import play.modules.reactivemongo.ReactiveMongoApi

import scala.util.{Failure, Success, Try}

/**
  * CIK Number Update Actor
  * @author lawrence.daniels@gmail.com
  */
class CikNumberUpdateActor(reactiveMongoApi: ReactiveMongoApi) extends Actor with ActorLogging {
  implicit val ec = context.dispatcher
  private val updateDAO = SecuritiesUpdateDAO(reactiveMongoApi)
  private val counter = new AtomicInteger()

  override def receive = {
    case missingCik: MissingCik =>
      processCikUpdate(missingCik)

    case UpdateMissingCikNumbers =>
      counter.set(0)
      val mySender = sender()
      findMissingCikSymbols() foreach { symbols =>
        symbols foreach (self ! _)
        mySender ! symbols.length
      }

    case message =>
      log.error(s"Unhandled message: $message (${Option(message).map(_.getClass.getName).orNull}})")
      unhandled(message)
  }

  private def findMissingCikSymbols() = {
    log.info(s"Searching for records missing CIK information...")
    val missingCiks = updateDAO.findMissingCiks
    missingCiks foreach { records =>
      log.info(s"Retrieved ${records.length} securities with missing CIK information")
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
    updateDAO.updateCik(symbol, name, cik) foreach { _ =>
      // log the statistics
      if (counter.incrementAndGet() % 20 == 0) {
        log.info(s"Processed ${counter.get} CIKs")
      }
    }
  }

}

/**
  * CIK Number Update Actor
  * @author lawrence.daniels@gmail.com
  */
object CikNumberUpdateActor {

  case object UpdateMissingCikNumbers

}
