package com.shocktrade.daycycle.daemons

import com.shocktrade.common.dao.securities.SecuritiesUpdateDAO._
import com.shocktrade.concurrent.ConcurrentProcessor
import com.shocktrade.concurrent.ConcurrentProcessor.ConcurrentContext
import com.shocktrade.concurrent.daemon.{BulkConcurrentTaskUpdateHandler, Daemon}
import com.shocktrade.daycycle.daemons.FullMarketUpdateDaemon._
import com.shocktrade.services.{EodDataSecuritiesService, LoggerFactory}
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.mongodb.Db
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Full Market Update Daemon (supports AMEX, NASDAQ, NYSE and OTCBB)
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class FullMarketUpdateDaemon(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) extends Daemon {
  private val logger = LoggerFactory.getLogger(getClass)

  // DAO and service instances
  private val securitiesDAO = dbFuture.flatMap(_.getSecuritiesUpdateDAO)
  private val eodDataService = new EodDataSecuritiesService()

  // internal variables
  private val processor = new ConcurrentProcessor()

  /**
    * Executes the process
    */
  def run(): Unit = {
    val startTime = js.Date.now()
    val inputs = getInputPages
    val outcome = processor.start(inputs, concurrency = 20, handler = new BulkConcurrentTaskUpdateHandler[InputPages](inputs.size) {
      logger.info(s"Scheduling ${inputs.size} pages of securities for processing...")

      override def onNext(ctx: ConcurrentContext, inputData: InputPages) = {
        for {
          quotes <- eodDataService(inputData.exchange, inputData.letterCode)
          results <- securitiesDAO.flatMap(_.updateEodQuotes(quotes))
        } yield (quotes.size, results)
      }
    })

    outcome onComplete {
      case Success(stats) =>
        logger.info(s"$stats in %d seconds", (js.Date.now() - startTime) / 1000)
      case Failure(e) =>
        logger.error(s"Failed during processing: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  private def getInputPages: js.Array[InputPages] = {
    for {
      exchange <- js.Array("NASDAQ", "NYSE", "AMEX", "OTCBB")
      letterCode <- 'A' to 'Z'
    } yield InputPages(exchange, letterCode)
  }

}

/**
  * Full Market Update Daemon
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object FullMarketUpdateDaemon {

  /**
    * Represents an input object containing an exchange and letter code
    * @param exchange   the given exchange (e.g. "NYSE")
    * @param letterCode the given letter code; the first letter of the symbol (e.g. "A")
    */
  case class InputPages(exchange: String, letterCode: Char)

}
