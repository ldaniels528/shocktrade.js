package com.shocktrade.ingestion.daemons
package mockmarket

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.dao.{DataAccessObjectHelper, MySQLDAO}
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Random, Success}

/**
 * Mock Stock Update Daemon
 * @param options the given [[MySQLConnectionOptions]]
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class MockStockUpdateDaemon(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions) extends MySQLDAO(options) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val random = new Random()
  import random.nextDouble

  /**
   * Updates all stocks to simulate an active market
   */
  def run(): Unit = {
    logger.info("Starting Mock-Stock Update process...")
    val startTime = System.currentTimeMillis()
    val outcome = for {
      stocks <- findStocks
      refreshedStocks = stocks.map(randomize)
      count <- Future.sequence(refreshedStocks.toSeq.map(updateStock)).map(_.sum)
    } yield count

    outcome onComplete {
      case Success(count) =>
        val elapsedTime = System.currentTimeMillis() - startTime
        val rate = if (elapsedTime > 0) count / (elapsedTime / 1000.0) else count
        logger.info(f"Processed $count symbols in $elapsedTime msec [$rate%.1f rec/sec]")
      case Failure(e) =>
        logger.error("Failed while updating mock stocks...")
        e.printStackTrace()
    }
  }

  def randomize(stock: StockData): StockData = {
    stock.copy(
      lastTrade = for {high <- stock.high; low <- stock.low} yield nextDouble() * (high - low) + low,
      tradeDateTime = new js.Date()
    )
  }

  def findStocks: Future[js.Array[StockData]] = conn.queryFuture[StockData]("SELECT * FROM mock_stocks").map(_._1)

  def updateStock(stock: StockData): Future[Int] = {
    import stock._
    conn.executeFuture(
      """|REPLACE INTO mock_stocks (
         |  symbol, `exchange`, `name`, lastTrade, tradeDateTime, prevClose, `open`, `close`, high, low,
         |  spread, `change`, changePct, volume, avgVolume10Day, beta, cikNumber, sector, industry
         |) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
         |""".stripMargin,
      js.Array(symbol, exchange, name, lastTrade, tradeDateTime, prevClose, open, close, high, low,
        spread, change, changePct, volume, avgVolume10Day, beta, cikNumber, sector, industry).map(_.orNull)).map(_.affectedRows)
  }

}
