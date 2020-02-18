package com.shocktrade.server.dao.securities.mongo

import com.shocktrade.server.dao.securities.{KeyStatisticsData, SecuritiesUpdateDAO, SecurityRef, SecurityUpdateQuote}
import com.shocktrade.server.services.BarChartProfileService.BarChartProfile
import com.shocktrade.server.services.BloombergQuoteService.BloombergQuote
import com.shocktrade.server.services.CikLookupService.CikLookupResponse
import com.shocktrade.server.services.EodDataSecuritiesService.EodDataSecurity
import com.shocktrade.server.services.NASDAQCompanyListService.NASDAQCompanyInfo
import io.scalajs.npm.mongodb._

import scala.scalajs.js

/**
 * Securities Update DAO (MongoDB version)
 * @param dao the given [[Collection]]
 */
class SecuritiesUpdateDAOMongoDB(val dao: Collection) extends SecuritiesUpdateDAO {

  @inline
  def findSymbolsIfEmpty(field: String): js.Promise[js.Array[SecurityRef]] = {
    dao.find[SecurityRef](
      selector = doc("active" $eq true, "symbol" $ne null, $or(field $exists false, field $eq null)),
      projection = SecurityRef.Fields.toProjection)
      .sort(js.Array("symbol", 1))
      .toArray()
  }

  @inline
  def findSymbolsForFinanceUpdate(cutOffTime: js.Date): js.Promise[js.Array[SecurityRef]] = {
    dao.find[SecurityRef](
      selector = doc("active" $eq true, "symbol" $ne null /*, $or("yfCsvLastUpdated" $exists false, "yfCsvLastUpdated" $lt cutOffTime)*/),
      projection = SecurityRef.Fields.toProjection)
      .sort(js.Array("symbol", 1))
      .toArray()
  }

  @inline
  def findSymbolsForKeyStatisticsUpdate(cutOffTime: js.Date): js.Promise[js.Array[SecurityRef]] = {
    dao.find[SecurityRef](
      selector = doc("active" $eq true, "symbol" $ne null /*, "exchange" $in (js.Array("NASDAQ", "NYQ", "NYSE"))*//*, $or("yfCsvLastUpdated" $exists false, "yfCsvLastUpdated" $lt cutOffTime)*/),
      projection = SecurityRef.Fields.toProjection)
      .sort(js.Array("symbol", 1))
      .toArray()
  }

  @inline
  def updateBloomberg(symbol: String, data: BloombergQuote): js.Promise[UpdateWriteOpResultObject] = {
    dao.updateOne(
      filter = "symbol" $eq symbol,
      update = $set(
        "sector" -> data.detailedQuote.flatMap(_.bicsSector),
        "industry" -> data.detailedQuote.flatMap(_.bicsIndustry),
        "subIndustry" -> data.detailedQuote.flatMap(_.bicsSubIndustry)
      ))
  }

  @inline
  def updateCik(cik: CikLookupResponse): js.Promise[UpdateWriteOpResultObject] = {
    dao.updateOne(filter = "symbol" $eq cik.symbol, update = $set("cikNumber" -> cik.CIK))
  }

  @inline
  def updateCompanyInfo(companies: Seq[NASDAQCompanyInfo]): js.Promise[BulkWriteOpResultObject] = {
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
  def updateEodQuotes(quotes: Seq[EodDataSecurity]): js.Promise[BulkWriteOpResultObject] = {
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
  def updateKeyStatistics(keyStats: KeyStatisticsData): js.Promise[UpdateWriteOpResultObject] = {
    dao.updateOne(filter = "symbol" $eq keyStats.symbol, update = $set(keyStats))
  }

  @inline
  def updateProfile(profile: BarChartProfile): js.Promise[UpdateWriteOpResultObject] = {
    dao.updateOne(
      filter = "symbol" $eq profile.symbol,
      update = $set(
        "ceoPresident" -> profile.ceoPresident,
        "description" -> profile.description
      ))
  }

  @inline
  def updateSecurities(quotes: Seq[SecurityUpdateQuote]): js.Promise[BulkWriteOpResultObject] = {
    dao.bulkWrite(js.Array(quotes map { quote =>
      updateOne(
        filter = "symbol" $eq quote.symbol,
        update = $set(quote)
      )
    }: _*))
  }

}
