package com.shocktrade.server.actors

import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.ldaniels528.commons.helpers.OptionHelper._
import com.ldaniels528.commons.helpers.ResourceHelper._
import com.shocktrade.controllers.QuotesController._
import com.shocktrade.server.actors.FinraRegShoUpdateActor.{ProcessRegSHO, RegSHO, _}
import com.shocktrade.util.BSONHelper._
import org.joda.time.DateTime
import play.libs.Akka
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument => BS}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.language.postfixOps

/**
 * Finra Registration SHO Update Actor
 * @author lawrence.daniels@gmail.com
 */
class FinraRegShoUpdateActor() extends Actor with ActorLogging {
  implicit val ec = context.dispatcher
  private lazy val mc = db.collection[BSONCollection]("Stocks")

  override def receive = {
    case ProcessRegSHO(processDate) =>
      sender ! processData(processDate)

    case message =>
      log.error(s"Unhandled message: $message (${Option(message).map(_.getClass.getName).orNull}})")
      unhandled(message)
  }

  /**
   * Symbol|Security Name|Market Category|Reg SHO Threshold Flag|Rule 4320
   * IWSY|ImageWare Systems, Inc. Common Stock|u|Y|N
   * XIDEQ|Exide Technologies New Common Stock|u|Y|N
   * EPAZ|Epazz, Inc. NEW Common Stock|u|Y|N
   * SFOR|Strikeforce Technologies, Inc. Common Stock|u|Y|N
   * @return the number of records scheduled for processing
   */
  private def processData(processDate: Date) = {
    val url = getDataURL(processDate)

    try {
      new URL(url).openStream() use { in =>
        val lines = Source.fromInputStream(in).getLines()
        val header = lines.next().tokenize
        val dataSet = lines map (line => Map(header zip line.tokenize: _*)) map { dataMap =>
          RegSHO(
            dataMap.get("Symbol") orDie "Security Symbol not found",
            dataMap.get("Security Name") map (_.truncate("New Common Stock")) map (_.truncate("Common Stock")) orDie "Security Name not found",
            dataMap.get("Market Category"),
            dataMap.get("Reg SHO Threshold Flag") map (_ == "Y"),
            dataMap.get("Rule 4320") map (_ == "Y")
          )
        } toSeq

        // schedule the data for processing
        val task = Future.sequence(dataSet map { data =>
          for {
            result <- persistData(data)
          } yield (data, result)
        })

        // display the results
        task.foreach {
          _ foreach { case (reg, writeResult) =>
            log.info(s"${reg.symbol}: updateCount = ${writeResult.n}, error = ${writeResult.errmsg.orNull}")
          }
        }
        dataSet.length
      }
    } catch {
      case e: Exception =>
        log.error(e, s"Error processing $url")
        -1
    }
  }

  private def persistData(reg: RegSHO) = {
    mc.update(BS("symbol" -> reg.symbol),
      BS(
        "baseSymbol" -> reg.symbol.take(4),
        "name" -> reg.securityName,
        "exchange" -> "OTCBB",
        "assetClass" -> "Equity",
        "assetType" -> "Common Stock",
        "active" -> true,
        "yfDynLastUpdated" -> new DateTime().minusDays(1).toDate
      ), upsert = true)
  }

  private def getDataURL(date: Date) = {
    val dateString = new SimpleDateFormat("yyyyMMdd").format(date)
    val fileName = s"D:\\OTCE\\DownloadFiles\\SHO\\otc-thresh${dateString}_${dateString}2300.txt"
    s"http://otce.finra.org/RegSHO/DownloadFileStream?fileLocation=$fileName"
  }

}

/**
 * Finra Registration SHO Update Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object FinraRegShoUpdateActor {
  private val myActor = Akka.system.actorOf(Props[FinraRegShoUpdateActor], name = "RegSHOUpdate")

  def !(message: Any) = myActor ! message

  def ?(message: Any)(implicit ec: ExecutionContext, timeout: Timeout) = myActor ? message

  case class ProcessRegSHO(processDate: Date)

  case class RegSHO(securityName: String,
                    symbol: String,
                    marketCategory: Option[String],
                    regSHOThresholdFlag: Option[Boolean],
                    rule4320: Option[Boolean])

  /**
   * String Extensions
   * @param src the host string
   */
  implicit class MyStringExtensions(val src: String) extends AnyVal {

    def tokenize: Seq[String] = src.split("[|]") map (_.trim)

    def truncate(s: String) = src.toLowerCase.lastIndexOf(s.toLowerCase) match {
      case -1 => src
      case index => src.substring(0, index).trim
    }

  }

}
