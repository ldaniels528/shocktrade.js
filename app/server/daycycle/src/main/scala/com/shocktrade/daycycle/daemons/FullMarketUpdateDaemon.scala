package com.shocktrade.daycycle.daemons

import com.shocktrade.common.dao.securities.SecuritiesUpdateDAO._
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.concurrent.daemon.{BulkUpdateHandler, Daemon}
import com.shocktrade.concurrent.{ConcurrentContext, ConcurrentProcessor}
import com.shocktrade.daycycle.daemons.FullMarketUpdateDaemon._
import com.shocktrade.services.EodDataSecuritiesService.EodDataSecurity
import com.shocktrade.services.{EodDataSecuritiesService, LoggerFactory, TradingClock}
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
    * Indicates whether the daemon is eligible to be executed
    * @param tradingClock the given [[TradingClock trading clock]]
    * @return true, if the daemon is eligible to be executed
    */
  override def isReady(tradingClock: TradingClock) = !tradingClock.isTradingActive

  /**
    * Executes the process
    * @param tradingClock the given [[TradingClock trading clock]]
    */
  override def run(tradingClock: TradingClock): Unit = {
    val startTime = js.Date.now()
    val inputs = getInputPages
    val outcome = processor.start(inputs, concurrency = 20, handler = new BulkUpdateHandler[InputPages](inputs.size) {
      logger.info(s"Scheduling ${inputs.size} pages of securities for processing...")

      override def onNext(ctx: ConcurrentContext, inputData: InputPages) = {
        for {
          quotes <- eodDataService(inputData.exchange, inputData.letterCode)
          results <- securitiesDAO.flatMap(_.updateEodQuotes(quotes.map(_.toQuote)))
        } yield (quotes.size, results.toBulkWrite)
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

  implicit class EodSecurityExtensions(val eod: EodDataSecurity) extends AnyVal {

    @inline
    def toQuote = new ResearchQuote(
      symbol = eod.symbol,
      exchange = eod.exchange,
      name = eod.name,
      high = eod.high,
      low = eod.low,
      close = eod.close,
      volume = eod.volume,
      change = eod.change,
      changePct = eod.changePct,
      active = true
    )

  }

  /**
    * Represents an input object containing an exchange and letter code
    * @param exchange   the given exchange (e.g. "NYSE")
    * @param letterCode the given letter code; the first letter of the symbol (e.g. "A")
    */
  case class InputPages(exchange: String, letterCode: Char)

}
