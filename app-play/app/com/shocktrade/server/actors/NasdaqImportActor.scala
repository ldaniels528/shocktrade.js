package com.shocktrade.server.actors

import java.util.Date

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.server.actors.NasdaqImportActor.{NasdaqImport, NasdaqQuote}
import com.shocktrade.util.BSONHelper._
import play.api.Play.current
import play.api.libs.ws.WS
import play.libs.Akka
import reactivemongo.bson.{BSONDocument => BS}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success, Try}

/**
 * NASDAQ Import Actor
 * @author lawrence.daniels@gmail.com
 */
class NasdaqImportActor() extends Actor with ActorLogging {

  import context.dispatcher

  override def receive = {
    case NasdaqImport =>
      val mySender = sender()
      processData() foreach { outcome =>
        mySender ! outcome
      }
    case message =>
      log.error(s"Unhandled message: $message (${Option(message).map(_.getClass.getName).orNull}})")
      unhandled(message)
  }

  /**
   * Processes the data from the NASDAQ site
   */
  private def processData()(implicit ec: ExecutionContext): Future[(Int, Int, Int)] = {
    Future.sequence {
      ('A' to 'Z') map { letter =>
        val urlString = s"http://www.nasdaq.com/screening/companies-by-name.aspx?letter=$letter&pagesize=2000&render=download"
        WS.url(urlString).get() map { response =>
          response.status match {
            case 200 =>
              // get the lines of text, and skip the header line
              val lines = Source.fromString(response.body).getLines()
              val headers = parseCSV(lines.next()) map (_.toLowerCase)

              // perform the updates
              var updates = 0
              var skipped = 0
              var errors = 0
              lines foreach { line =>
                parse(headers, line) match {
                  case Success(quote) =>
                    if (!quote.symbol.contains("^")) {
                      //log.info(s"Saving: ${quote.symbol} - ipoYear: ${quote.ipoYear.orNull} sector: ${quote.sector.orNull} industry: ${quote.industry.orNull}")
                      updateQuote(quote)
                      updates += 1
                    }
                    else skipped += 1
                  case Failure(e) =>
                    log.error(s"Failed to process: $line", e)
                    errors += 1
                }
              }

              log.info(s"NASDAQ('$letter') => (updates=$updates, skipped=$skipped, errors=$errors)")
              (updates, skipped, errors)
            case statusCode =>
              log.error(s"Received HTTP/$statusCode from $urlString")
              (0, 0, 0)
          }
        }
      }
    } map {
      _.foldLeft[(Int, Int, Int)]((0, 0, 0)) { case ((totalUpdates, totalSkipped, totalErrors), (updates, skipped, errors)) =>
        (totalUpdates + updates, totalSkipped + skipped, totalErrors + errors)
      }
    }
  }

  private def updateQuote(q: NasdaqQuote): Unit = {
    StockQuotes.updateQuote(q.symbol, BS(
      "name" -> q.name,
      "lastTrade" -> q.lastSale,
      "marketCap" -> q.marketCap,
      "ipoYear" -> q.ipoYear,
      "sector" -> q.sector,
      "industry" -> q.industry,
      "summaryQuote" -> q.summaryQuote,
      "active" -> true,
      "assetType" -> "Common Stock",
      "NasdaqLastUpdated" -> new Date()
    ))
  }

  /**
   * Parses the given line of text
   * "Symbol","Name","LastSale","MarketCap","ADR TSO","IPOyear","Sector","Industry","Summary Quote"
   * @param line the given line of text
   */
  private def parse(headers: List[String], line: String) = Try {
    val values = parseCSV(line) map (_.trim)
    val kvps = Map(headers zip values: _*)

    NasdaqQuote(
      symbol = kvps("symbol"),
      name = kvps.get("name"),
      lastSale = kvps.get("lastsale") flatMap clean map (_.toDouble),
      marketCap = kvps.get("marketcap") flatMap toDollars,
      adrTso = kvps.get("adr tso") flatMap clean,
      ipoYear = kvps.get("ipoyear") flatMap clean map (_.toInt),
      sector = kvps.get("sector") flatMap clean,
      industry = kvps.get("industry") flatMap clean,
      summaryQuote = kvps.get("summary quote") flatMap clean
    )
  }

  private def parseCSV(text: String) = {
    val sb = new StringBuilder()
    var inQuotes = false
    val tokens = text.toCharArray.foldLeft[List[String]](Nil) { (list, ch) =>
      val result = ch match {
        case ',' if !inQuotes =>
          val s = sb.toString()
          sb.clear()
          Some(s)
        case '"' =>
          inQuotes = !inQuotes
          None
        case c =>
          sb.append(c)
          None
      }
      result.map(_ :: list) getOrElse list
    }

    (if (sb.nonEmpty) sb.toString() :: tokens else tokens).reverse
  }

  private def toDollars(value: String): Option[Double] = {
    clean(value) map (_.replaceAllLiterally("$", "")) map {
      case s if s.last == 'K' => s.dropRight(1).toDouble * 1e+3
      case s if s.last == 'M' => s.dropRight(1).toDouble * 1e+6
      case s if s.last == 'B' => s.dropRight(1).toDouble * 1e+9
      case s if s.last == 'T' => s.dropRight(1).toDouble * 1e+12
      case s => s.toDouble
    }
  }

  private def clean(value: String): Option[String] = if (value.toLowerCase == "n/a") None else Some(value)

}

/**
 * NASDAQ Import Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object NasdaqImportActor {
  private val myActor = Akka.system.actorOf(Props[NasdaqImportActor], name = "NasdaqImport")

  def !(message: Any) = myActor ! message

  def ?(message: Any)(implicit ec: ExecutionContext, timeout: Timeout) = myActor ? message

  case object NasdaqImport

  case class NasdaqQuote(symbol: String,
                         name: Option[String],
                         lastSale: Option[Double],
                         marketCap: Option[Double],
                         adrTso: Option[String],
                         ipoYear: Option[Int],
                         sector: Option[String],
                         industry: Option[String],
                         summaryQuote: Option[String])

}
