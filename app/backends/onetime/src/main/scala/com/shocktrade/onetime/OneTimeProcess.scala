package com.shocktrade.onetime

import java.io.{FileOutputStream, PrintWriter}

import com.shocktrade.onetime.util.ResourceHelper._
import org.slf4j.LoggerFactory

import scala.io.Source

/**
 * One-time Process
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OneTimeProcess {
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    logger.info("ShockTrade One-time Process")

    new PrintWriter(new FileOutputStream("./temp/stockinfo.csv")) as { out =>
      Source.fromFile("./temp/stockinfo.txt").getLines() foreach { line =>
        out.println(toCSV(line))
      }
    }
  }

  def toCSV(line: String): String = {
    line.split("\t").map(_.trim).toList.mkString("|")
  }

}
