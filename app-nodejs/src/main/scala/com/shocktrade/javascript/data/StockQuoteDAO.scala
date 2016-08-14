package com.shocktrade.javascript.data

import com.shocktrade.javascript.forms.ResearchSearchOptions
import com.shocktrade.javascript.models.quote._
import org.scalajs.nodejs.mongodb._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.scalajs.js

/**
  * Stock Quote DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait StockQuoteDAO extends Collection

/**
  * Stock Quote DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object StockQuoteDAO {
  private val assetTypes = js.Array("Common Stock", "ETF")

  /**
    * Stock Quote DAO Extensions
    * @param quoteDAO the given [[StockQuoteDAO quoteDAO]]
    */
  implicit class QuoteDAOExtensions(val quoteDAO: StockQuoteDAO) extends AnyVal {

    @inline
    def exploreIndustries(sector: String)(implicit ec: ExecutionContext) = {
      quoteDAO.aggregate(js.Array(
        $match("active" $eq true, "assetType" $in assetTypes, "sector" $eq sector, "industry" $ne null),
        $group("_id" -> "$industry", "total" $sum 1)
      )).toArrayFuture[ExploreQuote]
    }

    @inline
    def exploreSectors(implicit ec: ExecutionContext) = {
      quoteDAO.aggregate(js.Array(
        $match("active" $eq true, "assetType" $in assetTypes, "sector" $ne null),
        $group("_id" -> "$sector", "total" $sum 1)
      )).toArrayFuture[ExploreQuote]
    }

    @inline
    def exploreSubIndustries(sector: String, industry: String)(implicit ec: ExecutionContext) = {
      quoteDAO.aggregate(js.Array(
        $match("active" $eq true, "assetType" $in assetTypes, "sector" $eq sector, "industry" $eq industry, "subIndustry" $ne null),
        $group("_id" -> "$subIndustry", "total" $sum 1)
      )).toArrayFuture[ExploreQuote]
    }

    @inline
    def findBasicQuote(symbol: String)(implicit ec: ExecutionContext) = {
      quoteDAO.findOneFuture[BasicQuote]("symbol" $eq symbol, fields = BasicQuote.Fields)
    }

    @inline
    def findFullQuote(symbol: String)(implicit ec: ExecutionContext) = {
      quoteDAO.findOneFuture[FullQuote]("symbol" $eq symbol)
    }

    @inline
    def findQuotesByIndustry(sector: String, industry: String, subIndustry: String)(implicit ec: ExecutionContext) = {
      val query = doc("active" $eq true, "assetType" $in assetTypes, "sector" $eq sector, "industry" $eq industry, "subIndustry" $eq subIndustry)
      quoteDAO.find(query, projection = BasicQuote.Fields.toProjection).toArrayFuture[BasicQuote]
    }

    @inline
    def findSectorInfo(symbol: String)(implicit ec: ExecutionContext) = {
      quoteDAO.findOneFuture[SectorInfoQuote]("symbol" $eq symbol, fields = SectorInfoQuote.Fields)
    }

    @inline
    def research(options: ResearchSearchOptions)(implicit ec: ExecutionContext) = {
      // build the query
      val selector = doc("active" $eq true, "symbol" $ne null)
      toRange("changePct", options.changeMin, options.changeMax) foreach (selector ++= _)
      toRange("lastTrade", options.priceMin, options.priceMax) foreach (selector ++= _)
      toRange("spread", options.spreadMin, options.spreadMax) foreach (selector ++= _)
      toRange("volume", options.volumeMin, options.volumeMax) foreach (selector ++= _)

      // determine the maximum number of results, the sort field and sort direction
      val maxResults = options.maxResults.flat.getOrElse(25)
      val sortField = options.sortBy.flat.getOrElse("symbol")
      val sortDirection = if (options.reverse.isTrue) -1 else 1

      // perform the query
      quoteDAO.find(selector, projection = ResearchQuote.Fields.toProjection)
        .limit(maxResults)
        .sort(js.Array(sortField, sortDirection))
        .toArrayFuture[ResearchQuote]
    }

    @inline
    def search(searchTerm: String, maxResults: Int)(implicit ec: ExecutionContext) = {
      quoteDAO.find(
        // { active : true, $or : [ {symbol : { $regex: ^?0, $options:'i' }}, {name : { $regex: ^?0, $options:'i' }} ] }
        selector = doc(
          "active" $eq true, "symbol" $ne null,
          $or("symbol" $regex(s"^$searchTerm", ignoreCase = true), "name" $regex(s"^$searchTerm", ignoreCase = true))
        ),
        projection = AutoCompleteQuote.Fields.toProjection)
        .sort(js.Array("name", 1))
        .limit(maxResults)
        .toArrayFuture[AutoCompleteQuote]
    }

    @inline
    private def toRange(field: String, minValue: js.UndefOr[Double], maxValue: js.UndefOr[Double]) = {
      (minValue.flat.toOption, maxValue.flat.toOption) match {
        case (Some(min), Some(max)) => Some(doc(field between minValue -> maxValue))
        case (Some(min), None) => Some(doc(field $gte min))
        case (None, Some(max)) => Some(doc(field $lte max))
        case (None, None) => None
      }
    }

  }

  /**
    * Stock Quote DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class QuoteDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getQuoteDAO(implicit ec: ExecutionContext) = db.collectionFuture("Stocks").mapTo[StockQuoteDAO]

  }

}