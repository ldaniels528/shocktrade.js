package com.shocktrade.models.quote

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.Json.{obj => JS, _}
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}

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
    var js = JS("active" -> true /*, "$or" -> JsArray(Seq(
      JS("tradeDate" -> JS("$gte" -> tenDaysAgo)),
      JS("tradeDateTime" -> JS("$gte" -> tenDaysAgo))
    ))*/)

    changeMin.foreach(v => js = js ++ JS("changePct" -> JS("$gte" -> v)))
    changeMax.foreach(v => js = js ++ JS("changePct" -> JS("$lte" -> v)))
    marketCapMin.foreach(v => js = js ++ JS("marketCap" -> JS("$gte" -> v)))
    marketCapMax.foreach(v => js = js ++ JS("marketCap" -> JS("$lte" -> v)))
    priceMin.foreach(v => js = js ++ JS("lastTrade" -> JS("$gte" -> v)))
    priceMax.foreach(v => js = js ++ JS("lastTrade" -> JS("$lte" -> v)))
    spreadMin.foreach(v => js = js ++ JS("spread" -> JS("$gte" -> v)))
    spreadMax.foreach(v => js = js ++ JS("spread" -> JS("$lte" -> v)))
    volumeMin.foreach(v => js = js ++ JS("volume" -> JS("$gte" -> v)))
    volumeMax.foreach(v => js = js ++ JS("volume" -> JS("$lte" -> v)))
    Logger.info(s"query: $js")
    js
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

}