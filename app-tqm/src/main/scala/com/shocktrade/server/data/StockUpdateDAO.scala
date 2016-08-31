package com.shocktrade.server.data

import com.shocktrade.server.services.YahooFinanceCSVQuotesService.YFCSVQuote
import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Stock Update DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait StockUpdateDAO extends Collection

/**
  * Stock Update DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object StockUpdateDAO {

  /**
    * Represents a stock quote reference
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait StockQuoteRef extends js.Object {
    var _id: ObjectID = js.native
    var symbol: String = js.native
    var exchange: String = js.native
  }

  /**
    * Stock Update DAO Extensions
    * @param stockDAO the given [[StockUpdateDAO Stock DAO]]
    */
  implicit class StockUpdateDAOExtensions(val stockDAO: StockUpdateDAO) extends AnyVal {

    @inline
    def findSymbolsForUpdate() = {
      stockDAO.find(
        selector = doc("active" $eq true, "symbol" $ne null, "exchange" $ne null),
        projection = js.Array("symbol", "exchange").toProjection)
        .sort(js.Array("symbol", 1))
        .toArrayFuture[StockQuoteRef]
    }

    @inline
    def updateQuote(quote: YFCSVQuote) = {
      stockDAO.updateOne(
        filter = "symbol" $eq quote.symbol,
        update = $set(
          "exchange" -> quote.exchange,
          "lastTrade" -> quote.lastTrade,
          "open" -> quote.open,
          "close" -> quote.close,
          "tradeDate" -> quote.tradeDate,
          "tradeTime" -> quote.tradeTime,
          "tradeDateTime" -> quote.tradeDateTime,
          "volume" -> quote.volume,
          "errorMessage" -> quote.errorMessage,
          "yfCsvResponseTime" -> quote.responseTimeMsec,
          "yfCsvLastUpdated" -> js.Date.now()
        )
      )
    }

    @inline
    def updateQuotes(quotes: Seq[YFCSVQuote]) = {
      stockDAO.bulkWrite(js.Array(
        quotes map { quote =>
          updateOne(
            filter = "symbol" $eq quote.symbol,
            update = $set(
              "exchange" -> quote.exchange,
              "lastTrade" -> quote.lastTrade,
              "open" -> quote.open,
              "close" -> quote.close,
              "tradeDate" -> quote.tradeDate,
              "tradeTime" -> quote.tradeTime,
              "tradeDateTime" -> quote.tradeDateTime,
              "volume" -> quote.volume,
              "errorMessage" -> quote.errorMessage,
              "yfCsvResponseTime" -> quote.responseTimeMsec,
              "yfCsvLastUpdated" -> new js.Date()
            ))
        }: _*)
      )
    }

  }

  /**
    * Stock Update DAO Constructor
    * @param db the given [[Db database]]
    */
  implicit class StockUpdateDAOConstructor(val db: Db) extends AnyVal {

    @inline
    def getStockDAO(implicit ec: ExecutionContext) = db.collectionFuture("Stocks").mapTo[StockUpdateDAO]

  }

}