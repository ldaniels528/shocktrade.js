package com.shocktrade.common.dao
package securities

import com.shocktrade.server.services.BarChartProfileService.BarChartProfile
import com.shocktrade.server.services.BloombergQuoteService.BloombergQuote
import com.shocktrade.server.services.CikLookupService.CikLookupResponse
import com.shocktrade.server.services.EodDataSecuritiesService.EodDataSecurity
import com.shocktrade.server.services.NASDAQCompanyListService.NASDAQCompanyInfo
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
    def findSymbolsIfEmpty(field: String) = {
      dao.find(
        selector = doc("active" $eq true, "symbol" $ne null, $or(field $exists false, field $eq null)),
        projection = SecurityRef.Fields.toProjection)
        .sort(js.Array("symbol", 1))
        .toArrayFuture[SecurityRef]
    }

    @inline
    def findSymbolsForFinanceUpdate(cutOffTime: js.Date) = {
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
    def updateBloomberg(symbol: String, data: BloombergQuote) = {
      dao.updateOne(
        filter = "symbol" $eq symbol,
        update = $set(
          "sector" -> data.detailedQuote.flatMap(_.bicsSector),
          "industry" -> data.detailedQuote.flatMap(_.bicsIndustry),
          "subIndustry" -> data.detailedQuote.flatMap(_.bicsSubIndustry)
        ))
    }

    @inline
    def updateCik(cik: CikLookupResponse) = {
      dao.updateOne(filter = "symbol" $eq cik.symbol, update = $set("cikNumber" -> cik.CIK))
    }

    @inline
    def updateCompanyInfo(companies: Seq[NASDAQCompanyInfo]) = {
      dao.bulkWrite(js.Array(companies map { company =>
        updateOne(
          filter = "symbol" $eq company.symbol,
          update = $set(
            "exchange" -> company.exchange,
            "name" -> company.name,
            "sector" -> company.sector,
            "industry" -> company.industry,
            "marketCap" -> company.marketCap,
            "IPOyear" -> company.IPOyear,
            "active" -> true
          ), upsert = true)
      }: _*))
    }

    @inline
    def updateEodQuotes(quotes: Seq[EodDataSecurity]) = {
      dao.bulkWrite(js.Array(quotes map { eod =>
        updateOne(
          filter = "symbol" $eq eod.symbol,
          update = $set(
            "exchange" -> eod.exchange,
            "name" -> eod.name,
            "high" -> eod.high,
            "low" -> eod.low,
            "close" -> eod.close,
            "volume" -> eod.volume,
            "change" -> eod.change,
            "changePct" -> eod.changePct,
            "active" -> true
          ),
          upsert = true
        )
      }: _*))
    }

    /**
      * Update the given key statistics data object
      * @param keyStats the given collection of [[KeyStatisticsData key statistics]] data objects
      * @return the promise of an [[BulkWriteOpResultObject bulk update result]]
      */
    @inline
    def updateKeyStatistics(keyStats: KeyStatisticsData)(implicit ec: ExecutionContext) = {
      dao.updateOne(filter = "symbol" $eq keyStats.symbol, update = $set(keyStats))
    }

    @inline
    def updateProfile(profile: BarChartProfile) = {
      dao.updateOne(
        filter = "symbol" $eq profile.symbol,
        update = $set(
          "ceoPresident" -> profile.ceoPresident,
          "description" -> profile.description
        ))
    }

    @inline
    def updateSecurities(quotes: Seq[SecurityUpdateQuote]) = {
      dao.bulkWrite(js.Array(quotes map { quote =>
        updateOne(
          filter = "symbol" $eq quote.symbol,
          update = $set(quote)
        )
      }: _*))
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