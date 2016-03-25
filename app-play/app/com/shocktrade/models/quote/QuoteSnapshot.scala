package com.shocktrade.models.quote

import java.util.Date

import play.api.libs.json.Json
import reactivemongo.bson.Macros

import scala.language.{implicitConversions, postfixOps}

/**
  * Quote Snapshot
  * @param name      the company name
  * @param symbol    the stock symbol/ticker
  * @param lastTrade the last sale amount
  * @param tradeDate the trade date
  * @author lawrence.daniels@gmail.com
  */
case class QuoteSnapshot(symbol: String,
                         name: Option[String],
                         lastTrade: Option[Double],
                         tradeDate: Option[Date])

/**
  * Quote Snapshot Singleton
  * @author lawrence.daniels@gmail.com
  */
object QuoteSnapshot {
  val Fields = Seq("symbol", "name", "lastTrade", "tradeDateTime")

  implicit val QuoteSnapshotFormat = Json.format[QuoteSnapshot]

  implicit val QuoteSnapshotHandler = Macros.handler[QuoteSnapshot]

}
