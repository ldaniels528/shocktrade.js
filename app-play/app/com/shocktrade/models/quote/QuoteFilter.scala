package com.shocktrade.models.quote

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}
import reactivemongo.bson.{BSONDocument => BS, BSONNull, BSONDocumentReader}

/**
 * Represents a Stock Quote Filter
 * @author lawrence.daniels@gmail.com
 */
case class QuoteFilter(changeMin: Option[Double] = None,
                       changeMax: Option[Double] = None,
                       spreadMin: Option[Double] = None,
                       spreadMax: Option[Double] = None,
                       marketCapMin: Option[Double] = None,
                       marketCapMax: Option[Double] = None,
                       priceMin: Option[Double] = None,
                       priceMax: Option[Double] = None,
                       volumeMin: Option[Long] = None,
                       volumeMax: Option[Long] = None,
                       maxResults: Option[Int] = None) {

  def makeQuery = {
    val tenDaysAgo = new DateTime().minusDays(10)

    // start with active stocks whose last trade date was updated in the last 10 days
    var doc = BS("active" -> true, "symbol" -> BS("$ne" -> BSONNull) /*, "$or" -> JsArray(Seq(
      BS("tradeDate" -> BS("$gte" -> tenDaysAgo)),
      BS("tradeDateTime" -> BS("$gte" -> tenDaysAgo))
    ))*/)

    changeMin.foreach(v => doc = doc ++ BS("changePct" -> BS("$gte" -> v)))
    changeMax.foreach(v => doc = doc ++ BS("changePct" -> BS("$lte" -> v)))
    marketCapMin.foreach(v => doc = doc ++ BS("marketCap" -> BS("$gte" -> v)))
    marketCapMax.foreach(v => doc = doc ++ BS("marketCap" -> BS("$lte" -> v)))
    priceMin.foreach(v => doc = doc ++ BS("lastTrade" -> BS("$gte" -> v)))
    priceMax.foreach(v => doc = doc ++ BS("lastTrade" -> BS("$lte" -> v)))
    spreadMin.foreach(v => doc = doc ++ BS("spread" -> BS("$gte" -> v)))
    spreadMax.foreach(v => doc = doc ++ BS("spread" -> BS("$lte" -> v)))
    volumeMin.foreach(v => doc = doc ++ BS("volume" -> BS("$gte" -> v)))
    volumeMax.foreach(v => doc = doc ++ BS("volume" -> BS("$lte" -> v)))
    Logger.info(s"query: $doc")
    doc
  }

}

/**
 * Quote Filter Singleton
 * @author lawrence.daniels@gmail.com
 */
object QuoteFilter {

  implicit val quoteFilterReads: Reads[QuoteFilter] = (
    (__ \ "changeMin").readNullable[Double] and
      (__ \ "changeMax").readNullable[Double] and
      (__ \ "spreadMin").readNullable[Double] and
      (__ \ "spreadMax").readNullable[Double] and
      (__ \ "marketCapMin").readNullable[Double] and
      (__ \ "marketCapMax").readNullable[Double] and
      (__ \ "priceMin").readNullable[Double] and
      (__ \ "priceMax").readNullable[Double] and
      (__ \ "volumeMin").readNullable[Long] and
      (__ \ "volumeMax").readNullable[Long] and
      (__ \ "maxResults").readNullable[Int])(QuoteFilter.apply _)

  implicit object QuoteFilterReader extends BSONDocumentReader[QuoteFilter] {
    override def read(doc: BS) = QuoteFilter(
      doc.getAs[Double]("changeMin"),
      doc.getAs[Double]("changeMax"),
      doc.getAs[Double]("spreadMin"),
      doc.getAs[Double]("spreadMax"),
      doc.getAs[Double]("marketCapMin"),
      doc.getAs[Double]("marketCapMax"),
      doc.getAs[Double]("priceMin"),
      doc.getAs[Double]("priceMax"),
      doc.getAs[Long]("volumeMin"),
      doc.getAs[Long]("volumeMax"),
      doc.getAs[Int]("maxResults")
    )
  }

}