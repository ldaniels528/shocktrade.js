package com.shocktrade.common.dao
package securities

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote._
import com.shocktrade.server.common.LoggerFactory
import org.scalajs.nodejs.mongodb._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Securities DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait SecuritiesDAO extends Collection

/**
  * Securities DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SecuritiesDAO {

  /**
    * Securities DAO Enrichment
    * @param dao the given [[SecuritiesDAO securities DAO]]
    */
  implicit class SecuritiesDAOEnrichment(val dao: SecuritiesDAO) extends AnyVal {

    @inline
    def exploreIndustries(sector: String)(implicit ec: ExecutionContext) = {
      dao.aggregate(js.Array(
        $match("active" $eq true, "sector" $eq sector, "industry" $ne null),
        $group("_id" -> "$industry", "total" $sum 1)
      )).toArrayFuture[ExploreQuote]
    }

    @inline
    def exploreSectors(implicit ec: ExecutionContext) = {
      dao.aggregate(js.Array(
        $match("active" $eq true, "sector" $ne null),
        $group("_id" -> "$sector", "total" $sum 1)
      )).toArrayFuture[ExploreQuote]
    }

    @inline
    def exploreSubIndustries(sector: String, industry: String)(implicit ec: ExecutionContext) = {
      dao.aggregate(js.Array(
        $match("active" $eq true, "sector" $eq sector, "industry" $eq industry, "subIndustry" $ne null),
        $group("_id" -> "$subIndustry", "total" $sum 1)
      )).toArrayFuture[ExploreQuote]
    }

    @inline
    def findCompleteQuote(symbol: String)(implicit ec: ExecutionContext) = {
      dao.findOneFuture[CompleteQuote]("symbol" $eq symbol)
    }

    @inline
    def findQuotesByIndustry(sector: String, industry: String)(implicit ec: ExecutionContext) = {
      val query = doc("active" $eq true, "sector" $eq sector, "industry" $eq industry, $or("subIndustry" $eq null, "subIndustry" $eq "", "subIndustry" $exists false))
      dao.find(query, projection = ResearchQuote.Fields.toProjection).toArrayFuture[ResearchQuote]
    }

    @inline
    def findQuotesBySubIndustry(sector: String, industry: String, subIndustry: String)(implicit ec: ExecutionContext) = {
      val query = doc("active" $eq true, "sector" $eq sector, "industry" $eq industry, "subIndustry" $eq subIndustry)
      dao.find(query, projection = ResearchQuote.Fields.toProjection).toArrayFuture[ResearchQuote]
    }

    @inline
    def findQuote[T <: js.Any](symbol: String, fields: Seq[String])(implicit ec: ExecutionContext) = {
      dao.findOneFuture[T]("symbol" $eq symbol, fields = js.Array(fields: _*))
    }

    @inline
    def findQuotes[T <: js.Any](selector: js.Any, fields: Seq[String])(implicit ec: ExecutionContext) = {
      dao.find(selector, projection = fields.toProjection).toArrayFuture[T]
    }

    @inline
    def findQuotesBySymbols[T <: js.Any](symbols: Seq[String], fields: Seq[String])(implicit ec: ExecutionContext) = {
      dao.find("symbol" $in js.Array(symbols: _*), projection = fields.toProjection).toArrayFuture[T]
    }

    @inline
    def research(options: ResearchOptions)(implicit ec: ExecutionContext) = {
      // build the query
      val selector = doc("active" $eq true, "symbol" $ne null)
      toRange("beta", options.betaMin, options.betaMax) foreach (selector ++= _)
      toRange("changePct", options.changeMin, options.changeMax) foreach (selector ++= _)
      toRange("lastTrade", options.priceMin, options.priceMax) foreach (selector ++= _)
      toRange("spread", options.spreadMin, options.spreadMax) foreach (selector ++= _)
      toRange("volume", options.volumeMin, options.volumeMax) foreach (selector ++= _)
      toRange("avgVolume10Day", options.avgVolumeMin, options.avgVolumeMax) foreach (selector ++= _)
      LoggerFactory.getLogger(getClass()).info("query: %j", selector)

      // is there an array of sort fields?
      val sortFields: js.Array[js.Any] = options.sortFields map (_ flatMap { sf =>
        js.Array(sf.field, sf.direction).asInstanceOf[js.Array[js.Any]]
      }) getOrElse {
        val sortField = options.sortBy.flat.getOrElse("symbol")
        val sortDirection = if (options.reverse.isTrue) -1 else 1
        js.Array(sortField, sortDirection)
      }

      // determine the maximum number of results
      val maxResults = options.maxResults.flat.getOrElse(25)

      // perform the query
      dao.find(selector, projection = ResearchQuote.Fields.toProjection)
        .limit(maxResults)
        .sort(sortFields)
        .toArrayFuture[ResearchQuote]
    }

    @inline
    def search(searchTerm: String, maxResults: Int)(implicit ec: ExecutionContext) = {
      dao.find(
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
    * Securities DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class SecuritiesDAOConstructors(val db: Db) extends AnyVal {

    /**
      * Retrieves the Securities DAO instance
      * @return the [[SecuritiesDAO Securities DAO]] instance
      */
    @inline
    def getSecuritiesDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Securities").mapTo[SecuritiesDAO]
    }

  }

}