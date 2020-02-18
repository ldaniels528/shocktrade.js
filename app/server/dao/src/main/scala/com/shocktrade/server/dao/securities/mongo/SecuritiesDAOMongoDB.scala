package com.shocktrade.server.dao.securities.mongo

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.{AutoCompleteQuote, CompleteQuote, ExploreQuote, ResearchQuote}
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.dao.securities.SecuritiesDAO
import io.scalajs.npm.mongodb.{$group, $match, $or, Collection, doc, _}
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Securities DAO (MongoDB version)
 * @param conn the given [[Collection]]
 */
class SecuritiesDAOMongoDB(conn: Collection) extends SecuritiesDAO {

  def exploreIndustries(sector: String): Future[js.Array[ExploreQuote]] = {
    conn.aggregate[ExploreQuote](js.Array(
      $match("active" $eq true, "sector" $eq sector, "industry" $ne null),
      $group("_id" -> "$industry", "total" $sum 1)
    )).toArray().toFuture
  }

  def exploreSectors(implicit ec: ExecutionContext): Future[js.Array[ExploreQuote]] = {
    conn.aggregate[ExploreQuote](js.Array(
      $match("active" $eq true, "sector" $ne null),
      $group("_id" -> "$sector", "total" $sum 1)
    )).toArray().toFuture
  }

  def exploreSubIndustries(sector: String, industry: String): Future[js.Array[ExploreQuote]] = {
    conn.aggregate[ExploreQuote](js.Array(
      $match("active" $eq true, "sector" $eq sector, "industry" $eq industry, "subIndustry" $ne null),
      $group("_id" -> "$subIndustry", "total" $sum 1)
    )).toArray().toFuture
  }

  def findCompleteQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[CompleteQuote]] = {
    conn.findOneFuture[CompleteQuote]("symbol" $eq symbol)
  }

  def findQuotesByIndustry(sector: String, industry: String): Future[js.Array[ResearchQuote]] = {
    val query = doc("active" $eq true, "sector" $eq sector, "industry" $eq industry, $or("subIndustry" $eq null, "subIndustry" $eq "", "subIndustry" $exists false))
    conn.find[ResearchQuote](query, projection = ResearchQuote.Fields.toProjection).toArray().toFuture
  }

  def findQuotesBySubIndustry(sector: String, industry: String, subIndustry: String): Future[js.Array[ResearchQuote]] = {
    val query = doc("active" $eq true, "sector" $eq sector, "industry" $eq industry, "subIndustry" $eq subIndustry)
    conn.find[ResearchQuote](query, projection = ResearchQuote.Fields.toProjection).toArray().toFuture
  }

  def findQuote[T <: js.Any](symbol: String, fields: Seq[String])(implicit ec: ExecutionContext): Future[Option[T]] = {
    conn.findOneFuture[T]("symbol" $eq symbol, fields = js.Array(fields: _*))
  }

  def findQuotes[T <: js.Any](selector: js.Any, fields: Seq[String]): Future[js.Array[T]] = {
    conn.find[T](selector, projection = fields.toProjection).toArray().toFuture
  }

  def findQuotesBySymbols[T <: js.Any](symbols: Seq[String], fields: Seq[String]): Future[js.Array[T]] = {
    conn.find[T]("symbol" $in js.Array(symbols: _*), projection = fields.toProjection).toArray().toFuture
  }

  def research(options: ResearchOptions): Future[js.Array[ResearchQuote]] = {
    // build the query
    val selector = doc("active" $eq true, "symbol" $ne null)
    toRange("beta", options.betaMin, options.betaMax) foreach (selector ++= _)
    toRange("changePct", options.changeMin, options.changeMax) foreach (selector ++= _)
    toRange("lastTrade", options.priceMin, options.priceMax) foreach (selector ++= _)
    toRange("spread", options.spreadMin, options.spreadMax) foreach (selector ++= _)
    toRange("volume", options.volumeMin, options.volumeMax) foreach (selector ++= _)
    toRange("avgVolume10Day", options.avgVolumeMin, options.avgVolumeMax) foreach (selector ++= _)
    LoggerFactory.getLogger(getClass).info("query: %j", selector)

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
    conn.find[ResearchQuote](selector, projection = ResearchQuote.Fields.toProjection)
      .limit(maxResults)
      .sort(sortFields)
      .toArray()
      .toFuture
  }

  def search(searchTerm: String, maxResults: Int): Future[js.Array[AutoCompleteQuote]] = {
    conn.find[AutoCompleteQuote](
      // { active : true, $or : [ {symbol : { $regex: ^?0, $options:'i' }}, {name : { $regex: ^?0, $options:'i' }} ] }
      selector = doc(
        "active" $eq true, "symbol" $ne null,
        $or("symbol" $regex(s"^$searchTerm", ignoreCase = true), "name" $regex(s"^$searchTerm", ignoreCase = true))
      ),
      projection = AutoCompleteQuote.Fields.toProjection)
      .sort(js.Array("name", 1))
      .limit(maxResults)
      .toArray()
      .toFuture
  }

  private def toRange(field: String, minValue: js.UndefOr[Double], maxValue: js.UndefOr[Double]) = {
    (minValue.flat.toOption, maxValue.flat.toOption) match {
      case (Some(min), Some(max)) => Some(doc(field between minValue -> maxValue))
      case (Some(min), None) => Some(doc(field $gte min))
      case (None, Some(max)) => Some(doc(field $lte max))
      case (None, None) => None
    }
  }

}
