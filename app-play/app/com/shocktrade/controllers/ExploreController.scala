package com.shocktrade.controllers

import com.shocktrade.controllers.QuotesController._
import com.shocktrade.models.quote.{SectorQuote, StockQuotes}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.{obj => JS, _}
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, _}
import reactivemongo.core.commands.{Aggregate, GroupField, Match, SumValue}

/**
 * Explore Controller
 */
object ExploreController extends Controller with MongoController with Classifications {
  private val Stocks = "Stocks"
  lazy val mcQ = db.collection[BSONCollection](Stocks)

  def exploreSectors(userID: String) = Action.async {
    val results = for {
      quotes <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> BS("$ne" -> BSONNull))),
        GroupField("sector")("total" -> SumValue(1))))) map { results =>
        results.toSeq map (Json.toJson(_))
      }
    } yield quotes
    results map (js => Ok(JsArray(js)))
  }

  def exploreIndustries(userID: String, sector: String) = Action.async {
    val results = for {
      quotes <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> BS("$ne" -> BSONNull))),
        GroupField("industry")("total" -> SumValue(1))))) map { results =>
        results.toSeq map (Json.toJson(_))
      }
    } yield quotes
    results map (js => Ok(JsArray(js)))
  }

  def exploreSubIndustries(userID: String, sector: String, industry: String) = Action.async {
    val results = for {
      quotes <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> industry, "subIndustry" -> BS("$ne" -> BSONNull))),
        GroupField("subIndustry")("total" -> SumValue(1))))) map { results =>
        results.toSeq map (Json.toJson(_))
      }
    } yield quotes
    results map (js => Ok(JsArray(js)))
  }

  def exploreQuotesBySubIndustry(userID: String, sector: String, industry: String, subIndustry: String) = Action.async {
    val results = for {
      quotes <- mcQ.find(BS("active" -> true, "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> industry, "subIndustry" -> subIndustry), searchFields)
        .cursor[BS]()
        .collect[Seq]()
    } yield quotes
    results map (quotes => Ok(Json.toJson(quotes)))
  }

  def exploreNAICSSectors = Action.async {
    (for {
      codes <- naicsCodes
      results <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "naicsNumber" -> BS("$ne" -> BSONNull))),
        GroupField("naicsNumber")("total" -> SumValue(1))))) map (_ map { bs =>
        val naicsNumber = bs.getAs[Int]("naicsNumber")
        JS("label" -> naicsNumber.flatMap(codes.get))
      })
    } yield results) map (js => Ok(JsArray(js)))
  }

  def exploreSICSectors = Action.async {
    (for {
      codes <- sicCodes
      results <- db.command(Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "sicNumber" -> BS("$ne" -> BSONNull))),
        GroupField("sicNumber")("total" -> SumValue(1))))) map (_.toSeq map { bs =>
        val sicNumber = bs.getAs[Int]("sicNumber")
        JS("label" -> sicNumber.flatMap(codes.get))
      })
    } yield results) map (js => Ok(JsArray(js)))
  }

  def getSectorInfo(symbol: String) = Action.async {
    StockQuotes.findOne[SectorQuote](symbol)(SectorQuote.Fields: _*) map (quote => Ok(Json.toJson(quote)))
  }

}
