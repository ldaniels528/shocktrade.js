package com.shocktrade.services

import com.shocktrade.services.util.ParsingUtilities._

import scala.language.postfixOps
import scala.xml.XML

/**
 * Stock Encyclopedia ETF List Service
 * @author lawrence.daniels@gmail.com
 */
object StockEncyclopediaETFListService {

  /**
   * Retrieves a list of available ETF stocks
   */
  def getETFStockList: Option[Seq[EtfTicker]] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    // get the web page's content as a string
    val content = XML
      .withSAXParser(new SAXFactoryImpl().newSAXParser())
      .load(s"http://etf.stock-encyclopedia.com/category/stock-etfs.html").toString()

    // get the option list of tickers
    val tickerList = for {
    // get the index of the sub-categories
      index <- content.optionOf("ETFs within all subcategories:")

      // extract the collection of ETF tickers from the string
      tickers = for {
      // get each table row <TD>...<A>...</A></TD>
        (p0, p1) <- content.findMatches( """<TD><B><A HREF=""", "</TD>", index)

        // get the HTML <TD> tag
        htmlTD = content.substring(p0, p1)

        // parse each ticker
        ticker = for {
        // get the bounds of the <A> tag
          (a0, a1) <- htmlTD.tagContent("A")

          // get the text from inside <A>..</A>
          anchorText = htmlTD.substring(a0, a1)

          // get the bounds of the parentheses (...)
          (b0, b1) <- anchorText.indexOptionOf("(", ")")

          // get the symbol and fund name
          symbol = anchorText.substring(b0 + 1, b1 - 1).trim
          name = anchorText.substring(0, b0).trim
        } yield EtfTicker(symbol, name)
      } yield ticker
    } yield tickers.flatten
    tickerList
  }

  /**
   * Represents an ETF Ticker
   * @author lawrence.daniels@gmail.com
   */
  case class EtfTicker(symbol: String, name: String)

}