package com.shocktrade.onetime

import org.slf4j.LoggerFactory

import scala.io.Source

/**
 * One-time Process
 */
object OneTimeProcess {
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    logger.info("ShockTrade One-time Process")
    Source.fromFile("./temp/stockinfo.txt").getLines() foreach { line =>
      logger.info(line)
    }
  }

}
