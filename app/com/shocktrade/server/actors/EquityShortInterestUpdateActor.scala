package com.shocktrade.server.actors

import java.net.{URL, URLEncoder}
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.ldaniels528.commons.helpers.OptionHelper._
import com.ldaniels528.commons.helpers.ResourceHelper._
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.server.actors.EquityShortInterestUpdateActor.{EquityShortInterest, UpdateEquityShortInterest}
import com.shocktrade.util.BSONHelper._
import org.joda.time.DateTime
import play.libs.Akka
import reactivemongo.bson.{BSONDocument => BS}

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.language.postfixOps

/**
 * Equity Short Interest Update Actor
 * @author lawrence.daniels@gmail.com
 */
class EquityShortInterestUpdateActor() extends Actor with ActorLogging {
  implicit val ec = context.dispatcher
  private val counter = new AtomicInteger()

  override def receive = {
    case UpdateEquityShortInterest =>
      sender ! processData()

    case esi: EquityShortInterest =>
      persistData(esi)

    case message =>
      log.error(s"Unhandled message: $message (${Option(message).map(_.getClass.getName).orNull}})")
      unhandled(message)
  }

  /**
   * Security Name|Security Symbol|OTC Market|Current Shares Short|Previous Report Shares Short|Change in Shares Short from Previous Report|Percent Change from Previous Report|Average Daily Share Volume|Days to Cover
   * Aareal Bank AG AKT|AAALF|u|7409|131879|-124470|-94.38|153|48.42
   * Aareal Bank AG Unsponsored Ame|AAALY|u|200|200|0|0|8|25
   * Altin Ag Baar Namen-AKT|AABNF|u|20|20|0|0|0|999.99
   * AAC Technologies Holdings Inc|AACAF|u|14660946|13732643|928303|6.7
   */
  private def processData() = {
    val url = getDataURL(new Date())

    try {
      new URL(url).openStream() use { in =>
        val lines = Source.fromInputStream(in).getLines()
        val header = tokenize(lines.next())
        val dataSet = lines map (line => Map(header zip tokenize(line): _*)) map { dataMap =>
          EquityShortInterest(
            dataMap.get("Security Name") orDie "Security Name not found",
            dataMap.get("Security Symbol") orDie "Security Symbol not found",
            dataMap.get("OTC Market"),
            dataMap.get("Current Shares Short") map (_.toLong),
            dataMap.get("Previous Report Shares Short") map (_.toLong),
            dataMap.get("Change in Shares Short from Previous Report") map (_.toLong),
            dataMap.get("Percent Change from Previous Report") map (_.toLong),
            dataMap.get("Average Daily Share Volume") map (_.toLong),
            dataMap.get("Days to Cover") map (_.toDouble)
          )
        } toSeq

        // schedule the data for processing
        dataSet foreach (EquityShortInterestUpdateActor ! _)
        dataSet.length
      }
    } catch {
      case e: Exception =>
        log.error(e, s"Error processing $url")
        -1
    }
  }

  private def persistData(esi: EquityShortInterest): Unit = {
    StockQuotes.updateQuote(esi.securitySymbol, BS(
      "name" -> esi.securityName,
      "exchange" -> "OTCBB",
      "volume" -> esi.averageDailyShareVolume,
      "active" -> true,
      "yfDynLastUpdated" -> new DateTime().minusDays(1).toDate
    ))

    // log the statistics
    if (counter.incrementAndGet() % 1000 == 0) {
      log.info(s"Processed ${counter.get} quotes")
    }
  }

  private def tokenize(line: String) = line.split("[|]") map (_.trim)

  private def getDataURL(date: Date) = {
    val dateString = new SimpleDateFormat("yyyyddMM").format(date)
    val fileName = s"D:\\OTCE\\DownloadFiles\\ESI\\shrt$dateString.txt"
    s"http://otce.finra.org/ESI/DownloadFileStream?fileLocation=$fileName"
  }

}

/**
 * Equity Short Interest Update Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object EquityShortInterestUpdateActor {
  private val myActor = Akka.system.actorOf(Props[EquityShortInterestUpdateActor], name = "ESIUpdate")

  def !(message: Any) = myActor ! message

  def ?(message: Any)(implicit ec: ExecutionContext, timeout: Timeout) = myActor ? message

  case object UpdateEquityShortInterest

  case class EquityShortInterest(securityName: String,
                                 securitySymbol: String,
                                 otcMarket: Option[String],
                                 currentSharesShort: Option[Long],
                                 previousReportSharesShort: Option[Long],
                                 changeInSharesShortFromPreviousReport: Option[Long],
                                 percentChangeFromPreviousReport: Option[Long],
                                 averageDailyShareVolume: Option[Long],
                                 DaysToCover: Option[Double])

}
