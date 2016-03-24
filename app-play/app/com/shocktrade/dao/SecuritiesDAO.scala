package com.shocktrade.dao

import com.shocktrade.models.quote._
import com.shocktrade.util.BSONHelper._
import org.joda.time.DateTime
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, _}
import reactivemongo.core.commands.{Aggregate, GroupField, Match, SumValue}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Securities DAO
  * @author lawrence.daniels@gmail.com
  */
case class SecuritiesDAO(reactiveMongoApi: ReactiveMongoApi) extends Classifications {
  private val Stocks = "Stocks"
  private val db = reactiveMongoApi.db
  private val mcQ = db.collection[BSONCollection](Stocks)

  val limitFields = BS(
    "name" -> 1, "symbol" -> 1, "exchange" -> 1, "lastTrade" -> 1,
    "change" -> 1, "changePct" -> 1, "spread" -> 1, "volume" -> 1)

  val searchFields = BS(
    "name" -> 1, "symbol" -> 1, "exchange" -> 1, "lastTrade" -> 1, "change" -> 1, "changePct" -> 1,
    "open" -> 1, "close" -> 1, "high" -> 1, "low" -> 1, "tradeDate" -> 1, "spread" -> 1, "volume" -> 1)

  /**
    * Auto-completes symbols and company names
    */
  def autoComplete(searchTerm: String, maxResults: Int = 20)(implicit ec: ExecutionContext) = {
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

  def findByFilter(form: QuoteFilter, fields: Seq[String] = Nil, maxResults: Int = 20)(implicit ec: ExecutionContext) = {
    mcQ.find(form.makeQuery, fields.toBsonFields)
      .cursor[ResearchQuote]()
      .collect[Seq](maxResults)
  }

  def findChangeQuotes(symbols: Seq[String])(implicit ec: ExecutionContext) = {
    if (symbols.isEmpty) Future.successful(Seq.empty)
    else {
      mcQ.find(
        BS("symbol" -> BS("$in" -> symbols)),
        BS("name" -> 1, "symbol" -> 1, "exchange" -> 1, "lastTrade" -> 1, "changePct" -> 1, "volume" -> 1, "sector" -> 1, "industry" -> 1))
        .cursor[BS]().collect[Seq]()
    }
  }

  /**
    * Retrieves a complete quote; the composition of real-time quote and a disc-based quote
    * @param symbol the given ticker symbol
    * @return the [[Future promise]] of an option of a [[reactivemongo.bson.BSONDocument quote]]
    */
  def findFullQuote(symbol: String)(implicit ec: ExecutionContext) = {
    mcQ.find(BS("$or" -> BSONArray(Seq(BS("symbol" -> symbol), BS("changes.symbol" -> BS("$in" -> Seq(symbol))))))).one[BS]
  }

  def findOne[T](symbol: String)(fields: String*)(implicit ec: ExecutionContext, r: BSONDocumentReader[T]): Future[Option[T]] = {
    mcQ.find(BS("symbol" -> symbol), fields.toBsonFields).one[T]
  }

  def findQuotes[T](symbols: Seq[String])(fields: String*)(implicit ec: ExecutionContext, r: BSONDocumentReader[T]): Future[Seq[T]] = {
    mcQ.find(BS("symbol" -> BS("$in" -> symbols)), fields.toBsonFields).cursor[T]().collect[Seq]()
  }

  def findQuotesBySymbols(symbols: Seq[String])(implicit ec: ExecutionContext) = {
    mcQ.find(BS("symbol" -> BS("$in" -> symbols)), SectorQuote.Fields.toBsonFields)
      .cursor[SectorQuote]()
      .collect[Seq]()
  }

  def getExchangeCounts(implicit ec: ExecutionContext) = {
    db.command(Aggregate(Stocks, Seq(
      Match(BS("active" -> true, "exchange" -> BS("$ne" -> BSONNull), "assetType" -> BS("$in" -> BSONArray("Common Stock", "ETF")))),
      GroupField("exchange")("total" -> SumValue(1)))
    ))
  }

  /**
    * Retrieves pricing for a collection of symbols
    */
  def getPricing(symbols: Seq[String])(implicit ec: ExecutionContext) = {
    findQuotes[QuoteSnapshot](symbols)(QuoteSnapshot.Fields: _*)
  }

  def getRiskLevel(symbol: String)(implicit ec: ExecutionContext) = {
    for {
      quote_? <- mcQ.find(BS("symbol" -> symbol)).one[BS]
      result = quote_? match {
        case None => "Unknown"
        case Some(quote) =>
          val beta_? = quote.getAs[Double]("beta")
          beta_? match {
            case Some(beta) if beta >= 0 && beta <= 1.25 => "Low";
            case Some(beta) if beta > 1.25 && beta <= 1.9 => "Medium";
            case Some(beta) => "High"
            case None => "Unknown"
          }
      }
    } yield result
  }

  def getSymbolsForCsvUpdate(implicit ec: ExecutionContext): Cursor[BS] = {
    mcQ.find(BS("active" -> true, "$or" -> BSONArray(Seq(
      BS("yfDynLastUpdated" -> BS("$exists" -> false)),
      BS("yfDynLastUpdated" -> BS("$lte" -> new DateTime().minusMinutes(15)))
    ))), BS("symbol" -> 1)).cursor[BS]()
  }

  def getSymbolsForKeyStatisticsUpdate(implicit ec: ExecutionContext): Cursor[BS] = {
    mcQ.find(BS("active" -> true, "$or" -> BSONArray(Seq(
      BS("yfKeyStatsLastUpdated" -> BS("$exists" -> false)),
      BS("yfKeyStatsLastUpdated" -> BS("$lte" -> new DateTime().minusDays(2)))
    ))), BS("symbol" -> 1)).cursor[BS]()
  }

  def updateQuote(symbol: String, doc: BS)(implicit ec: ExecutionContext) = {
    mcQ.update(BS("symbol" -> symbol), BS("$set" -> doc))
  }

  def exploreSectors(userID: String)(implicit ec: ExecutionContext) = {
    for {
      quotes <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> BS("$ne" -> BSONNull))),
        GroupField("sector")("total" -> SumValue(1)))))
    } yield quotes
  }

  def exploreIndustries(userID: String, sector: String)(implicit ec: ExecutionContext) = {
    for {
      quotes <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> BS("$ne" -> BSONNull))),
        GroupField("industry")("total" -> SumValue(1)))))
    } yield quotes
  }

  def exploreSubIndustries(userID: String, sector: String, industry: String)(implicit ec: ExecutionContext) = {
    for {
      quotes <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> industry, "subIndustry" -> BS("$ne" -> BSONNull))),
        GroupField("subIndustry")("total" -> SumValue(1)))))
    } yield quotes
  }

  def exploreQuotesBySubIndustry(userID: String, sector: String, industry: String, subIndustry: String)(implicit ec: ExecutionContext) = {
    for {
      quotes <- mcQ.find(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> industry, "subIndustry" -> subIndustry), searchFields)
        .cursor[BS]()
        .collect[Seq]()
    } yield quotes
  }

  def exploreNAICSSectors(implicit ec: ExecutionContext) = {
    for {
      codes <- naicsCodes
      results <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "naicsNumber" -> BS("$ne" -> BSONNull))),
        GroupField("naicsNumber")("total" -> SumValue(1))))) map (_ map { bs =>
        val naicsNumber = bs.getAs[Int]("naicsNumber")
        BS("label" -> naicsNumber.flatMap(codes.get))
      })
    } yield results
  }

  def exploreSICSectors(implicit ec: ExecutionContext) = {
    for {
      codes <- sicCodes
      results <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "sicNumber" -> BS("$ne" -> BSONNull))),
        GroupField("sicNumber")("total" -> SumValue(1))))) map (_.toSeq map { bs =>
        val sicNumber = bs.getAs[Int]("sicNumber")
        BS("label" -> sicNumber.flatMap(codes.get))
      })
    } yield results
  }

  def getSectorInfo(symbol: String)(implicit ec: ExecutionContext) = {
    findOne[SectorQuote](symbol)(SectorQuote.Fields: _*)
  }

}
