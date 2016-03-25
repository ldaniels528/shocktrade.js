package com.shocktrade.dao

import com.shocktrade.models.quote._
import com.shocktrade.util.BSONHelper._
import org.joda.time.DateTime
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONArray, BSONDocument => BS, _}
import reactivemongo.core.commands.{Aggregate, GroupField, Match, SumValue}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Securities DAO MongoDB implementation
  * @author lawrence.daniels@gmail.com
  */
class SecuritiesDAOMongoDB(reactiveMongoApi: ReactiveMongoApi) extends SecuritiesDAO {
  private val db = reactiveMongoApi.db
  private val mcN = db.collection[BSONCollection]("NAICS")
  private val mcS = db.collection[BSONCollection]("SIC")
  private val mcQ = db.collection[BSONCollection]("Stocks")

  val limitFields = BS(
    "name" -> 1, "symbol" -> 1, "exchange" -> 1, "lastTrade" -> 1,
    "change" -> 1, "changePct" -> 1, "spread" -> 1, "volume" -> 1)

  val searchFields = BS(
    "name" -> 1, "symbol" -> 1, "exchange" -> 1, "lastTrade" -> 1, "change" -> 1, "changePct" -> 1,
    "open" -> 1, "close" -> 1, "high" -> 1, "low" -> 1, "tradeDate" -> 1, "spread" -> 1, "volume" -> 1)

  /**
    * Auto-completes symbols and company names
    */
  override def autoComplete(searchTerm: String, maxResults: Int = 20)(implicit ec: ExecutionContext) = {
    mcQ.find(
      // { active : true, $or : [ {symbol : { $regex: ^?0, $options:'i' }}, {name : { $regex: ?0, $options:'i' }} ] }
      BS("symbol" -> BS("$ne" -> BSONNull), "$or" -> BSONArray(Seq(
        BS("symbol" -> BS("$regex" -> s"^$searchTerm", "$options" -> "i")),
        BS("name" -> BS("$regex" -> s"^$searchTerm", "$options" -> "i"))))),
      // fields
      BS("symbol" -> 1, "name" -> 1, "exchange" -> 1, "assetType" -> 1))
      .sort(BS("symbol" -> 1))
      .cursor[AutoCompleteQuote]()
      .collect[Seq](maxResults)
  }

  override def findBasicQuotes(symbols: Seq[String])(implicit ec: ExecutionContext) = {
    mcQ.find(BS("symbol" -> BS("$in" -> symbols)), BasicQuote.Fields.toBsonFields).cursor[BasicQuote]().collect[Seq]()
  }

  override def findByFilter(form: QuoteFilter, fields: Seq[String] = Nil, maxResults: Int = 20)(implicit ec: ExecutionContext) = {
    mcQ.find(makeQuery(form), fields.toBsonFields)
      .cursor[ResearchQuote]()
      .collect[Seq](maxResults)
  }

  override def findProductQuotes(symbols: Seq[String])(implicit ec: ExecutionContext) = {
    if (symbols.isEmpty) Future.successful(Seq.empty)
    else {
      mcQ.find(
        BS("symbol" -> BS("$in" -> symbols)),
        BS("symbol" -> 1, "exchange" -> 1, "lastTrade" -> 1, "change" -> 1, "changePct" -> 1, "spread" -> 1, "volume" -> 1, "name" -> 1, "sector" -> 1, "industry" -> 1, "active" -> 1))
        .cursor[ProductQuote]().collect[Seq]()
    }
  }

  /**
    * Retrieves a complete quote; the composition of real-time quote and a disc-based quote
    * @param symbol the given ticker symbol
    * @return the [[Future promise]] of an option of a [[reactivemongo.bson.BSONDocument quote]]
    */
  override def findFullQuote(symbol: String)(implicit ec: ExecutionContext) = {
    mcQ.find(BS("$or" -> BSONArray(Seq(BS("symbol" -> symbol), BS("changes.symbol" -> BS("$in" -> Seq(symbol))))))).one[BS]
  }

  override def findQuotesBySymbols(symbols: Seq[String])(implicit ec: ExecutionContext) = {
    mcQ.find(BS("symbol" -> BS("$in" -> symbols)), SectorQuote.Fields.toBsonFields)
      .cursor[SectorQuote]()
      .collect[Seq]()
  }

  override def findExchangeSummaries(implicit ec: ExecutionContext) = {
    db.command(Aggregate("Stocks", Seq(
      Match(BS("active" -> true, "exchange" -> BS("$ne" -> BSONNull), "assetType" -> BS("$in" -> BSONArray("Common Stock", "ETF")))),
      GroupField("exchange")("total" -> SumValue(1)))
    )) map (_ flatMap (_.seeAsOpt[ExchangeSummary]))
  }

  /**
    * Retrieves pricing for a collection of symbols
    */
  override def findSnapshotQuotes(symbols: Seq[String])(implicit ec: ExecutionContext) = {
    findSnapshotQuotes(symbols)
  }

  override def findSectorQuote(symbol: String)(implicit ec: ExecutionContext) = {
    mcQ.find(BS("symbol" -> symbol), SectorQuote.Fields.toBsonFields).one[SectorQuote]
  }

  /**
    * Retrieves the NAICS code for the given NAICS number
    * @param naicsNumber the given NAICS number
    */
  override def findNaicsCodeByNumber(naicsNumber: Int)(implicit ec: ExecutionContext) = {
    mcN.find(BS("naicsNumber" -> naicsNumber)).one[NaicsCode]
  }

  /**
    * Retrieves the NAICS code for the given NAICS numbers
    * @param naicsNumbers the given collection of NAICS numbers
    */
  override def findNaicsCodeByNumbers(naicsNumbers: Seq[Int])(implicit ec: ExecutionContext) = {
    mcN.find(BS("naicsNumber" -> BS("$in" -> naicsNumbers))).cursor[NaicsCode]().collect[Seq]()
  }

  /**
    * Retrieves the SIC code for the given SIC number
    * @param sicNumber the given SIC number
    */
  override def findSicCodeByNumber(sicNumber: Int)(implicit ec: ExecutionContext) = {
    mcS.find(BS("sicNumber" -> sicNumber)).one[SicCode]
  }

  /**
    * Retrieves the SIC code for the given SIC numbers
    * @param sicNumbers the given collection SIC numbers
    */
  override def findSicCodeByNumbers(sicNumbers: Seq[Int])(implicit ec: ExecutionContext) = {
    mcS.find(BS("sicNumber" -> BS("$in" -> sicNumbers))).cursor[SicCode]().collect[Seq]()
  }

  private def makeQuery(filter: QuoteFilter) = {
    val tenDaysAgo = new DateTime().minusDays(10)

    // start with active stocks whose last trade date was updated in the last 10 days
    var doc = BS("active" -> true, "symbol" -> BS("$ne" -> BSONNull) /*, "$or" -> JsArray(Seq(
      BS("tradeDate" -> BS("$gte" -> tenDaysAgo)),
      BS("tradeDateTime" -> BS("$gte" -> tenDaysAgo))
    ))*/)

    filter.changeMin.foreach(v => doc = doc ++ BS("changePct" -> BS("$gte" -> v)))
    filter.changeMax.foreach(v => doc = doc ++ BS("changePct" -> BS("$lte" -> v)))
    filter.marketCapMin.foreach(v => doc = doc ++ BS("marketCap" -> BS("$gte" -> v)))
    filter.marketCapMax.foreach(v => doc = doc ++ BS("marketCap" -> BS("$lte" -> v)))
    filter.priceMin.foreach(v => doc = doc ++ BS("lastTrade" -> BS("$gte" -> v)))
    filter.priceMax.foreach(v => doc = doc ++ BS("lastTrade" -> BS("$lte" -> v)))
    filter.spreadMin.foreach(v => doc = doc ++ BS("spread" -> BS("$gte" -> v)))
    filter.spreadMax.foreach(v => doc = doc ++ BS("spread" -> BS("$lte" -> v)))
    filter.volumeMin.foreach(v => doc = doc ++ BS("volume" -> BS("$gte" -> v)))
    filter.volumeMax.foreach(v => doc = doc ++ BS("volume" -> BS("$lte" -> v)))
    Logger.info(s"query: $doc")
    doc
  }

}
