package com.shocktrade.dao

import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, _}
import reactivemongo.core.commands.{Aggregate, GroupField, Match, SumValue}

import scala.concurrent.ExecutionContext

/**
  * Securities Explorer DAO
  * @author lawrence.daniels@gmail.com
  */
class SecuritiesExplorerDAOMongoDB(reactiveMongoApi: ReactiveMongoApi) extends SecuritiesDAOMongoDB(reactiveMongoApi)
  with SecuritiesExplorerDAO {

  private val Stocks = "Stocks"
  private val db = reactiveMongoApi.db
  private val mcQ = db.collection[BSONCollection](Stocks)

  override def exploreSectors(userID: String)(implicit ec: ExecutionContext) = {
    for {
      quotes <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> BS("$ne" -> BSONNull))),
        GroupField("sector")("total" -> SumValue(1)))))
    } yield quotes
  }

  override def exploreIndustries(userID: String, sector: String)(implicit ec: ExecutionContext) = {
    for {
      quotes <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> BS("$ne" -> BSONNull))),
        GroupField("industry")("total" -> SumValue(1)))))
    } yield quotes
  }

  override def exploreSubIndustries(userID: String, sector: String, industry: String)(implicit ec: ExecutionContext) = {
    for {
      quotes <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> industry, "subIndustry" -> BS("$ne" -> BSONNull))),
        GroupField("subIndustry")("total" -> SumValue(1)))))
    } yield quotes
  }

  override def exploreQuotesBySubIndustry(userID: String, sector: String, industry: String, subIndustry: String)(implicit ec: ExecutionContext) = {
    for {
      quotes <- mcQ.find(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> industry, "subIndustry" -> subIndustry), searchFields)
        .cursor[BS]()
        .collect[Seq]()
    } yield quotes
  }

  override def exploreNAICSSectors(implicit ec: ExecutionContext) = {
    for {
      results <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "naicsNumber" -> BS("$ne" -> BSONNull))),
        GroupField("naicsNumber")("total" -> SumValue(1)))))
      naicsNumbers = results.flatMap(_.getAs[Int]("naicsNumber"))
      sectors <- findNaicsCodeByNumbers(naicsNumbers) map (_ map (naics => BS("label" -> naics.description)))
    } yield sectors
  }

  override def exploreSICSectors(implicit ec: ExecutionContext) = {
    for {
      results <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "sicNumber" -> BS("$ne" -> BSONNull))),
        GroupField("sicNumber")("total" -> SumValue(1)))))
      sicNumbers = results.flatMap(_.getAs[Int]("sicNumber"))
      sectors <- findSicCodeByNumbers(sicNumbers) map (_ map (sic => BS("label" -> sic.description)))
    } yield sectors
  }

}
