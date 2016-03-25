package com.shocktrade.services.yahoofinance

import com.shocktrade.services.HttpUtil

import scala.concurrent.{ExecutionContext, Future}

/**
 * Yahoo! Finance: Symbol Directory Service
 * @author lawrence.daniels@gmail.com
 */
object YFSymbolSuggestionService extends HttpUtil {
  private lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  /**
   * This service attempt to find the symbol for the specified company name
   * @param query the specified query (e.g., "Paragon")
   * @return the list of search results
   */
  def search(query: String)(implicit ec: ExecutionContext): Future[Seq[YFSearchResult]] = {

    // capture the start time
    val startTime = System.currentTimeMillis()

    // define the service URL
    Future {
      getResource(s"http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=$query&callback=YAHOO.Finance.SymbolSuggest.ssCallback")
    } map (bytes => transform(new String(bytes), startTime))
  }

  /**
   * Transforms the JSON string into an object graph
   */
  def transform(jsonString: String, startTime: Long): Seq[YFSearchResult] = {
    import net.liftweb.json._
    implicit val formats = DefaultFormats

    // capture the response time (in milliseconds)
    val responseTimeMsec = System.currentTimeMillis() - startTime

    // remove the extraneous callback
    val jsonCleanString = stripCallbackWrapper(jsonString)

    // transform the JSON string into a JValue
    val json = parse(jsonCleanString)

    // transform the JValue into a YFSymbolSuggestion instance
    val graph = json.extract[YFSymbolSuggestion]

    // TODO create a de-normalized object

    graph.results
  }

  private def stripCallbackWrapper(jsonString: String) = {
    val startSeq = """"ResultSet":"""
    val endSeq = "})"
    (jsonString.indexOf(startSeq), jsonString.lastIndexOf(endSeq)) match {
      case (start, end) if start != -1 && end != -1 =>
        jsonString.substring(start + startSeq.length, end)
          .replace("\"Query\":", "\"query\":")
          .replace("\"Result\":", "\"results\":")
      case _ => ""
    }
  }

  case class YFSymbolSuggestion(query: String, results: List[YFSearchResult])

  case class YFSearchResult(symbol: String, name: String, exch: String, `type`: String, exchDisp: String, typeDisp: String)

}