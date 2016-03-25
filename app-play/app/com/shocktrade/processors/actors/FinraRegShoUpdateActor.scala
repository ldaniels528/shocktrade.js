package com.shocktrade.processors.actors

import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.{Actor, ActorLogging}
import com.github.ldaniels528.commons.helpers.OptionHelper._
import com.github.ldaniels528.commons.helpers.ResourceHelper._
import com.shocktrade.dao.SecuritiesDAO
import com.shocktrade.processors.actors.FinraRegShoUpdateActor.{ProcessRegSHO, RegSHO, _}
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.Future
import scala.io.Source
import scala.language.postfixOps

/**
  * Finra Registration SHO Update Actor
  * @author lawrence.daniels@gmail.com
  */
class FinraRegShoUpdateActor(reactiveMongoApi: ReactiveMongoApi) extends Actor with ActorLogging {
  implicit val ec = context.dispatcher
  private val securitiesDAO = SecuritiesDAO(reactiveMongoApi)

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
            result <- securitiesDAO.updateRegSHO(data)
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
