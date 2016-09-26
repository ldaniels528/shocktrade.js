package com.shocktrade.common.dao
package securities

import com.shocktrade.services.NASDAQCompanyListService.NASDAQCompanyInfo
import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Securities Update DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait SecuritiesUpdateDAO extends SecuritiesDAO

/**
  * Stock Update DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SecuritiesUpdateDAO {

  /**
    * Stock Update DAO Enrichment
    * @param dao the given [[SecuritiesUpdateDAO Stock DAO]]
    */
  implicit class SecuritiesUpdateDAOEnrichment(val dao: SecuritiesUpdateDAO) extends AnyVal {

    @inline
    def findSymbolsForCikUpdate() = {
      dao.find(
        selector = doc("active" $eq true, "symbol" $ne null, $or("cikNumber" $exists false, "cikNumber" $eq null)),
        projection = SecurityRef.Fields.toProjection)
        .sort(js.Array("symbol", 1))
        .toArrayFuture[SecurityRef]
    }

    @inline
    def findSymbolsForUpdate(cutOffTime: js.Date) = {
      dao.find(
        selector = doc("active" $eq true, "symbol" $ne null /*, $or("yfCsvLastUpdated" $exists false, "yfCsvLastUpdated" $lt cutOffTime)*/),
        projection = SecurityRef.Fields.toProjection)
        .sort(js.Array("symbol", 1))
        .toArrayFuture[SecurityRef]
    }

    @inline
    def findSymbolsForKeyStatisticsUpdate(cutOffTime: js.Date) = {
      dao.find(
        selector = doc("active" $eq true, "symbol" $ne null /*, "exchange" $in (js.Array("NASDAQ", "NYQ", "NYSE"))*//*, $or("yfCsvLastUpdated" $exists false, "yfCsvLastUpdated" $lt cutOffTime)*/),
        projection = SecurityRef.Fields.toProjection)
        .sort(js.Array("symbol", 1))
        .toArrayFuture[SecurityRef]
    }

    @inline
    def updateCik(symbol: String, cik: String) = {
      dao.updateOne(
        filter = "symbol" $eq symbol,
        update = $set("cikNumber" -> cik)
      )
    }

    @inline
    def updateCompanyInfo(companies: Seq[NASDAQCompanyInfo]) = {
      dao.bulkWrite(
        js.Array(companies map { company =>
          updateOne(filter = "symbol" $eq company.symbol, update = $set(
            "symbol" -> company.symbol,
            "exchange" -> company.exchange,
            "name" -> company.name,
            "sector" -> company.sector,
            "industry" -> company.industry,
            "marketCap" -> company.marketCap,
            "IPOyear" -> company.IPOyear,
            "active" -> true
          ), upsert = true)
        }: _*)
      )
    }

    @inline
    def updateQuote(quote: SecurityUpdateQuote) = {
      dao.updateOne(
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
          "yfCsvResponseTime" -> quote.yfCsvResponseTime,
          "yfCsvLastUpdated" -> quote.yfCsvLastUpdated
        )
      )
    }

    @inline
    def updateQuotes(quotes: Seq[SecurityUpdateQuote]) = {
      dao.bulkWrite(js.Array(
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
              "yfCsvResponseTime" -> quote.yfCsvResponseTime,
              "yfCsvLastUpdated" -> quote.yfCsvLastUpdated
            ))
        }: _*)
      )
    }

  }

  /**
    * Securities Update DAO Constructor
    * @param db the given [[Db database]]
    */
  implicit class SecuritiesUpdateDAOConstructor(val db: Db) extends AnyVal {

    @inline
    def getSecuritiesUpdateDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Securities").mapTo[SecuritiesUpdateDAO]
    }

  }

}