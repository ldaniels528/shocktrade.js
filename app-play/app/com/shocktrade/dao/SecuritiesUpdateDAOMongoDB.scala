package com.shocktrade.dao

import com.shocktrade.processors.actors.FinraRegShoUpdateActor.RegSHO
import com.shocktrade.processors.actors.MissingCik
import com.shocktrade.services.CikCompanySearchService.CikInfo
import com.shocktrade.util.BSONHelper._
import org.joda.time.DateTime
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONArray, BSONDocument => BS, _}

import scala.concurrent.ExecutionContext

/**
  * Securities Update DAO (MongoDB implementation)
  * @author lawrence.daniels@gmail.com
  */
case class SecuritiesUpdateDAOMongoDB(reactiveMongoApi: ReactiveMongoApi) extends SecuritiesUpdateDAO {
  private val mcQ = reactiveMongoApi.db.collection[BSONCollection]("Stocks")

  override def findMissingCiks(implicit ec: ExecutionContext) = {
    // query the missing symbols
    // db.Stocks.count({"active":true, "assetType":"Common Stock", "name":{"$ne":null}, "cikNumber":{"$exists":false}});
    // db.Stocks.find({"active":true, "assetType":"Common Stock", "name":{"$ne":null}, "cikNumber":{"$exists":false}});
    mcQ.find(
      BS("active" -> true, "assetType" -> "Common Stock", "name" -> BS("$ne" -> BSONNull), "cikNumber" -> BS("$exists" -> false)),
      BS("symbol" -> 1, "name" -> 1))
      .cursor[MissingCik]()
      .collect[Seq]()
  }

  override def getSymbolsForCsvUpdate(implicit ec: ExecutionContext): Cursor[BS] = {
    mcQ.find(BS("active" -> true, "$or" -> BSONArray(Seq(
      BS("yfDynLastUpdated" -> BS("$exists" -> false)),
      BS("yfDynLastUpdated" -> BS("$lte" -> new DateTime().minusMinutes(15)))
    ))), BS("symbol" -> 1)).cursor[BS]()
  }

  override def getSymbolsForKeyStatisticsUpdate(implicit ec: ExecutionContext): Cursor[BS] = {
    mcQ.find(BS("active" -> true, "$or" -> BSONArray(Seq(
      BS("yfKeyStatsLastUpdated" -> BS("$exists" -> false)),
      BS("yfKeyStatsLastUpdated" -> BS("$lte" -> new DateTime().minusDays(2)))
    ))), BS("symbol" -> 1)).cursor[BS]()
  }

  override def updateQuote(symbol: String, doc: BS)(implicit ec: ExecutionContext) = {
    mcQ.update(BS("symbol" -> symbol), BS("$set" -> doc)) map (_.nModified)
  }

  override def updateCik(symbol: String, name: String, cik: CikInfo)(implicit ec: ExecutionContext) = {
    import cik._

    mcQ.update(
      BS("symbol" -> symbol),
      if (name.length < cikName.length)
        BS("$set" -> BS("name" -> cikName, "cikNumber" -> cikNumber))
      else
        BS("$set" -> BS("cikNumber" -> cikNumber)),
      upsert = false, multi = false) map (_.nModified)
  }

  override def updateRegSHO(reg: RegSHO)(implicit ec: ExecutionContext) = {
    mcQ.update(BS("symbol" -> reg.symbol),
      BS(
        "baseSymbol" -> reg.symbol.take(4),
        "name" -> reg.securityName,
        "exchange" -> "OTCBB",
        "assetClass" -> "Equity",
        "assetType" -> "Common Stock",
        "active" -> true,
        "yfDynLastUpdated" -> new DateTime().minusDays(1).toDate
      ), upsert = true) map (_.nModified)
  }

}
