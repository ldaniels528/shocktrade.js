package com.shocktrade.services.yahoofinance

import java.text.SimpleDateFormat
import java.util.Date

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{NodeSeq, XML}

/**
 * Yahoo! Finance: Option Quote Service
 * @author lawrence.daniels@gmail.com
 */
object YFOptionQuoteService extends YFWebService {

  /**
   * Retrieves a quote for the given option symbol
   * @param symbol the given option symbol (e.g. "AAPL")
   * @return a { @link Future future} of a sequence of { @link YFOptionQuote option quotes}
   */
  def getQuotes(symbol: String)(implicit ec: ExecutionContext): Future[Seq[YFOptionQuote]] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // create a locally shared date formatter
    implicit val sdf = new SimpleDateFormat("'Expire at close' E, MMM dd, yyyy")

    // capture the service start time
    val startTime = System.currentTimeMillis()

    // retrieve & parse the document
    Future {
      XML
        .withSAXParser(new SAXFactoryImpl().newSAXParser())
        .load(s"http://finance.yahoo.com/q/op?s=$symbol+Options")
    } map { doc =>
      parseDocument(symbol, doc, startTime)
    }
  }

  /**
   * Transforms the given node sequences to Option objects
   */
  private def parseDocument(symbol: String, html: NodeSeq, startTime: Long)(implicit sdf: SimpleDateFormat): Seq[YFOptionQuote] = {
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // parse all qualifying table sequences
    val results: Seq[OptionItem] = (html \\ "table") flatMap { table =>
      val tableClass = table \ "@class"
      if (tableClass.exists(_.text == "yfnc_datamodoutline1")) Some(parseOptionData(table))
      else if (tableClass.exists(_.text == "yfnc_mod_table_title1")) Some(parseOptionHeader(table))
      else None
    }

    // transform the results into option quotes
    results.sliding(2, 2).toSeq flatMap {
      case Seq(info: OptionHeader, data: OptionData, _*) =>
        data.mappings map (m => toQuote(symbol, info, m, responseTimeMsec))
    }
  }

  private def parseOptionData(table: NodeSeq): OptionData = {
    // weed our way down the chain to the inner table
    val rows = table \ "tr" \ "td" \ "table" \ "tr"

    // create the sequence of headings
    val headerCols = (rows.head \ "th") map (_.text.trim)

    // create each row of data
    val results = rows.tail map { node =>
      // create the sequence of items        
      val dataCols = (node \ "td") map (_.text.trim)
      Map(headerCols zip dataCols flatMap {
        // remove blank values
        case (key, value) =>
          if (value == "" || value == "N/A") None else Some((key, value))
      }: _*)
    }
    OptionData(results)
  }

  private def parseOptionHeader(table: NodeSeq)(implicit sdf: SimpleDateFormat): OptionHeader = {
    (table \ "tr" \ "td") map (_.text) match {
      case Seq(optionType, expiry, _*) => OptionHeader(Some(optionType), Some(sdf.parse(expiry)))
      case Seq(optionType, _*) => OptionHeader(Some(optionType), None)
      case _ => OptionHeader(None, None)
    }
  }

  private def toQuote(symbol: String, info: OptionHeader, m: Map[String, String], responseTimeMsec: Long) = {
    YFOptionQuote(
      symbol,
      info.optionType,
      info.expiration,
      m.get("Symbol"),
      m.get("Strike") flatMap asNumber,
      m.get("Open Int") flatMap asInt,
      m.get("Last") flatMap asNumber,
      m.get("Chg") flatMap asNumber,
      m.get("Ask") flatMap asNumber,
      m.get("Bid") flatMap asNumber,
      m.get("Vol") flatMap asLong,
      responseTimeMsec)
  }

  sealed trait OptionItem

  case class OptionData(mappings: Seq[Map[String, String]]) extends OptionItem

  case class OptionHeader(optionType: Option[String], expiration: Option[Date]) extends OptionItem

  case class YFOptionQuote(
                            baseSymbol: String,
                            optionType: Option[String],
                            expiration: Option[Date],
                            symbol: Option[String],
                            strike: Option[Double],
                            openInt: Option[Int],
                            last: Option[Double],
                            change: Option[Double],
                            ask: Option[Double],
                            bid: Option[Double],
                            volume: Option[Long],
                            responseTimeMsec: Long)

}
