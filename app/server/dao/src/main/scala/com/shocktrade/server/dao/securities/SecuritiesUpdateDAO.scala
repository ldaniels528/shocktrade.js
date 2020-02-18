package com.shocktrade.server.dao
package securities

import com.shocktrade.server.dao.securities.mongo.SecuritiesUpdateDAOMongoDB
import com.shocktrade.server.services.BarChartProfileService.BarChartProfile
import com.shocktrade.server.services.BloombergQuoteService.BloombergQuote
import com.shocktrade.server.services.CikLookupService.CikLookupResponse
import com.shocktrade.server.services.EodDataSecuritiesService.EodDataSecurity
import com.shocktrade.server.services.NASDAQCompanyListService.NASDAQCompanyInfo
import io.scalajs.npm.mongodb._

import scala.scalajs.js

/**
  * Securities Update DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait SecuritiesUpdateDAO {

  def findSymbolsIfEmpty(field: String): js.Promise[js.Array[SecurityRef]]

  def findSymbolsForFinanceUpdate(cutOffTime: js.Date): js.Promise[js.Array[SecurityRef]]

  def findSymbolsForKeyStatisticsUpdate(cutOffTime: js.Date): js.Promise[js.Array[SecurityRef]]

  def updateBloomberg(symbol: String, data: BloombergQuote): js.Promise[UpdateWriteOpResultObject]

  def updateCik(cik: CikLookupResponse): js.Promise[UpdateWriteOpResultObject]

  def updateCompanyInfo(companies: Seq[NASDAQCompanyInfo]): js.Promise[BulkWriteOpResultObject]

  def updateEodQuotes(quotes: Seq[EodDataSecurity]): js.Promise[BulkWriteOpResultObject]

  /**
   * Update the given key statistics data object
   * @param keyStats the given collection of [[KeyStatisticsData key statistics]] data objects
   * @return the promise of an [[BulkWriteOpResultObject bulk update result]]
   */
  def updateKeyStatistics(keyStats: KeyStatisticsData): js.Promise[UpdateWriteOpResultObject]

  def updateProfile(profile: BarChartProfile): js.Promise[UpdateWriteOpResultObject]

  def updateSecurities(quotes: Seq[SecurityUpdateQuote]): js.Promise[BulkWriteOpResultObject]

}

/**
  * Stock Update DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SecuritiesUpdateDAO {

  def apply(db: Db): SecuritiesUpdateDAOMongoDB = new SecuritiesUpdateDAOMongoDB(db.collection("Securities"))

}