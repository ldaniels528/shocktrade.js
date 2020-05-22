package com.shocktrade.ingestion.daemons
package mockmarket

import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.common.models.quote.Ticker
import com.shocktrade.remote.proxies.RemoteEventProxy
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.dao.{DataAccessObjectHelper, MySQLDAO}
import io.scalajs.npm.mysql.MySQLConnectionOptions
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Mock Stock Update Daemon
 * @param options the given [[MySQLConnectionOptions]]
 */
class MockStockUpdateDaemon(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions) extends MySQLDAO(options) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val restProxy = new RemoteEventProxy(host = "localhost", port = 9000)
  import StockTrends._

  // watch these symbols
  StockTrends.watch("AAPL", "TSLA", "TACOW", "AMZN")

  /**
   * Updates all stocks to simulate an active market
   */
  def run(): Unit = {
    val startTime = System.currentTimeMillis()
    val outcome = for {
      stocks <- findStocks
      refreshedStocks = stocks.map(refreshStock)
      count <- saveStocks(refreshedStocks)
    } yield (stocks, refreshedStocks, count)

    outcome onComplete {
      case Success((stocks, refreshedStocks, count)) =>
        val elapsedTime = System.currentTimeMillis() - startTime
        val rate = if (elapsedTime > 0) count / (elapsedTime / 1000.0) else count
        logger.info(f"Processed $count symbols in $elapsedTime msec [$rate%.1f rec/sec]")
      case Failure(e) =>
        logger.error("Failed while updating mock stocks...")
        e.printStackTrace()
    }
  }

  private def findStocks: Future[js.Array[StockData]] = {
    conn.queryFuture[StockData]("SELECT * FROM mock_stocks WHERE isActive = 1 ORDER BY tradeDateTime").map(_._1)
  }

  private def refreshStock(stock: StockData): StockData = {
    (for {
      newSale <- getNewSale(stock)
      high <- stock.high.map(Math.max(newSale, _))
      low <- stock.low.map(Math.min(newSale, _))
      prevClose <- stock.prevClose
      volume <- stock.volume ?? 0.0

      change = newSale - prevClose
      changePct = 100.0 * (change / prevClose)
      spread: js.UndefOr[Double] = if (high != 0) 100.0 * ((high - low) / high) else js.undefined

    } yield stock.copy(
      change = change, changePct = changePct, lastTrade = newSale, spread = spread, volume = volume,
      high = high, low = low, tradeDateTime = new js.Date
    )) getOrElse {
      logger.warn(s"Problem with stock ${stock.symbol.orNull}")
      stock
    }
  }

  private def saveStocks(stockList: Seq[StockData]): Future[Int] = {
    val batchSize = 1500
    Future.sequence(stockList.sliding(batchSize, batchSize).toSeq map { stocks =>
      conn.executeFuture(
        s"""|REPLACE INTO mock_stocks (
            |  symbol, `exchange`, `name`, lastTrade, tradeDateTime, prevClose, `open`, `close`, high, low, highLimit, lowLimit,
            |  spread, `change`, changePct, volume, avgVolume10Day, beta, cikNumber, sector, industry
            |) VALUES ${stocks map { _ => "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" } mkString ",\n"}
            |""".stripMargin,
        stocks flatMap { stock =>
          import stock._
          Seq(symbol, exchange, name, lastTrade, tradeDateTime, prevClose, open, close, high, low, highLimit, lowLimit,
            spread, change, changePct, volume, avgVolume10Day, beta, cikNumber, sector, industry).map(_.orNull)
        }).map(_.affectedRows)
    }).map(_.sum)
  }

  private def transmitUpdates(stocks: js.Array[StockData]): Unit = {
    try {
      logger.info(s"Transmitting ${stocks.length} stocks to relay...")
      val startTime = js.Date.now()
      val outcome = restProxy.relayEvent(RemoteEvent.createStockUpdateEvent(tickers = stocks.map { stock =>
        import stock._
        new Ticker(symbol = symbol, exchange = exchange, lastTrade = lastTrade, tradeDateTime = tradeDateTime)
      }))

      outcome onComplete {
        case Success(_) =>
          val elapsedTime = js.Date.now() - startTime
          logger.info(s"Completed in $elapsedTime msec")
        case Failure(e) =>
          val elapsedTime = js.Date.now() - startTime
          logger.error(s"Failed after $elapsedTime msec: ${e.getMessage}")
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    ()
  }

}
