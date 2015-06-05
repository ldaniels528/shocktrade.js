package com.shocktrade.models.quote

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, _}

/**
 * Represents a real-time quote
 * @author lawrence.daniels@gmail.com
 */
case class CompleteQuote(symbol: String,
                         name: Option[String] = None,
                         exchange: Option[String] = None,
                         lastTrade: Option[Double] = None,
                         tradeDateTime: Option[Date] = None,
                         change: Option[Double] = None,
                         changePct: Option[Double] = None,
                         prevClose: Option[Double] = None,
                         open: Option[Double] = None,
                         close: Option[Double] = None,
                         low: Option[Double] = None,
                         high: Option[Double] = None,
                         low52Week: Option[Double] = None,
                         high52Week: Option[Double] = None,
                         volume: Option[Long] = None,
                         avgVol3m: Option[Long] = None,
                         marketCap: Option[Double] = None,
                         active: Option[Boolean] = None,
                         //products: List[ETFProduct] = Nil,
                         askBid: AskBid = AskBid(),
                         industryCodes: IndustryCodes = IndustryCodes(),
                         valuation: Valuation = Valuation()) {

  def spreadPct: Option[Double] = for {
    l <- low
    h <- high
  } yield (h - l) / l

}

/**
 * Complete Quote Singleton
 * @author lawrence.daniels@gmail.com
 */
object CompleteQuote {

  implicit val completeQuoteReads: Reads[CompleteQuote] = {
    ((__ \ "symbol").read[String] and
      (__ \ "name").readNullable[String] and
      (__ \ "exchange").readNullable[String] and
      (__ \ "lastTrade").readNullable[Double] and
      (__ \ "tradeDateTime" \ "$date").readNullable[Long].map(_.map(t => new Date(t))) and
      (__ \ "change").readNullable[Double] and
      (__ \ "changePct").readNullable[Double] and
      (__ \ "prevClose").readNullable[Double] and
      (__ \ "open").readNullable[Double] and
      (__ \ "close").readNullable[Double] and
      (__ \ "low").readNullable[Double] and
      (__ \ "high").readNullable[Double] and
      (__ \ "low52Week").readNullable[Double] and
      (__ \ "high52Week").readNullable[Double] and
      (__ \ "volume").readNullable[Long] and
      (__ \ "avgVol3m").readNullable[Long] and
      (__ \ "marketCap").readNullable[Double] and
      (__ \ "active").readNullable[Boolean] and
      //(__ \ "products").read[List[ETFProduct]] and
      __.read[AskBid] and
      __.read[IndustryCodes] and
      __.read[Valuation])(CompleteQuote.apply _)
  }

  implicit val completeQuoteWrites: Writes[CompleteQuote] = (
    (__ \ "symbol").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "exchange").writeNullable[String] and
      (__ \ "lastTrade").writeNullable[Double] and
      (__ \ "tradeDateTime").writeNullable[Date] and
      (__ \ "change").writeNullable[Double] and
      (__ \ "changePct").writeNullable[Double] and
      (__ \ "prevClose").writeNullable[Double] and
      (__ \ "open").writeNullable[Double] and
      (__ \ "close").writeNullable[Double] and
      (__ \ "low").writeNullable[Double] and
      (__ \ "high").writeNullable[Double] and
      (__ \ "low52Week").writeNullable[Double] and
      (__ \ "high52Week").writeNullable[Double] and
      (__ \ "volume").writeNullable[Long] and
      (__ \ "avgVol3m").writeNullable[Long] and
      (__ \ "marketCap").writeNullable[Double] and
      (__ \ "active").writeNullable[Boolean] and
      //(__ \ "products").write[List[ETFProduct]] and
      __.write[AskBid] and
      __.write[IndustryCodes] and
      __.write[Valuation])(unlift(CompleteQuote.unapply))

  implicit object CompleteQuoteReader extends BSONDocumentReader[CompleteQuote] {
    def read(doc: BSONDocument) = CompleteQuote(
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("name"),
      doc.getAs[String]("exchange"),
      doc.getAs[Double]("lastTrade"),
      doc.getAs[Date]("tradeDateTime"),
      doc.getAs[Double]("change"),
      doc.getAs[Double]("changePct"),
      doc.getAs[Double]("prevClose"),
      doc.getAs[Double]("open"),
      doc.getAs[Double]("close"),
      doc.getAs[Double]("low"),
      doc.getAs[Double]("high"),
      doc.getAs[Double]("low52Week"),
      doc.getAs[Double]("high52Week"),
      doc.getAs[Long]("volume"),
      doc.getAs[Long]("avgVol3m"),
      doc.getAs[Double]("marketCap"),
      doc.getAs[Boolean]("active"),
      //doc.getAs[List[ETFProduct]]("products").getOrElse(Nil),
      AskBid.AskBidReader.read(doc),
      IndustryCodes.IndustryCodesReader.read(doc),
      Valuation.ValuationReader.read(doc)
    )
  }

  implicit object CompleteQuoteWriter extends BSONDocumentWriter[CompleteQuote] {
    def write(quote: CompleteQuote) = BSONDocument(
      "symbol" -> quote.symbol,
      "name" -> quote.name,
      "exchange" -> quote.exchange,
      "lastTrade" -> quote.lastTrade,
      "tradeDateTime" -> quote.tradeDateTime,
      "change" -> quote.change,
      "changePct" -> quote.changePct,
      "prevClose" -> quote.prevClose,
      "open" -> quote.open,
      "close" -> quote.close,
      "low" -> quote.low,
      "high" -> quote.high,
      "low52Week" -> quote.low52Week,
      "high52Week" -> quote.high52Week,
      "volume" -> quote.volume,
      "avgVol3m" -> quote.avgVol3m,
      "marketCap" -> quote.marketCap,
      "active" -> quote.active,
      // collections
      //"products" -> quote.products,
      "ask" -> quote.askBid.ask,
      "askSize" -> quote.askBid.askSize,
      "bid" -> quote.askBid.bid,
      "bidSize" -> quote.askBid.bidSize,
      "naicsNumber" -> quote.industryCodes.naicsNumber,
      "sicNumber" -> quote.industryCodes.sicNumber,
      "beta" -> quote.valuation.beta,
      "target1Yr" -> quote.valuation.target1Yr,
      // computed values
      "spreadPct" -> quote.spreadPct
    )
  }

}

