package com.shocktrade.services.directory

import com.shocktrade.services.util._
import com.shocktrade.services.yahoofinance.{YFBusinessProfileService, YFCurrencyQuoteService, YFRealtimeStockQuoteService}

/**
 * The service directory provides a means for discovering the service (or services)
 * that are required to fulfill a data request
 * @author lawrence.daniels@gmail.com
 */
class DefaultServiceDirectory() extends ServiceDirectory {
  import DefaultServiceDirectory._
  import ServiceDirectory._

  import scala.concurrent.ExecutionContext

  // let's create our service domains
  "Currencies" ~> Seq(CxYFCurrencySource())
  "Quotes" ~> Seq(CxYFRealtimeStockQuoteSource(), CxYFBusinessProfileSource())

  /**
   * Selection DSL - works just like SQL queries
   */
  def get(domainName: String, fields: Set[String], conditions: Seq[(String, Any)])(implicit ec: ExecutionContext) = {

    def grab[S](domainName: String) = getDomain[S](domainName).getOrElse(throw new IllegalArgumentException(s"Domain '$domainName' not found"))

    domainName match {
      case "Currencies" => grab[CurrencyQuote](domainName).fetch(fields, conditions)
      case "Quotes" => grab[CompleteQuote](domainName).fetch(fields, conditions)
      case _ =>
        throw new IllegalArgumentException(s"Domain '$domainName' not found")
    }
  }

  def executeQuery[S](query: String)(implicit ec: ExecutionContext, m: Manifest[S]) = {

    def fail[S]: S = {
      throw new IllegalArgumentException(
        "Syntax error - usage: select [fields] from [domain] where [condition]\n" +
          "Example: select symbol, lastTrade from Quotes where symbol = \"AAPL\"")
    }

    // parse the query
    val params = parseQuery(query.toString)

    // get the indices of "from" and "where"
    (params.indexWhere(_ == "from"), params.indexWhere(_ == "where")) match {
      case (p0, p1) if p0 != -1 && p1 != -1 && p1 > p0 =>
        // extract the arguments
        val fields = params.slice(1, p0)
        val domainName = params.slice(p0 + 1, p1).headOption getOrElse fail
        val condition = (params.slice(p1 + 1, params.length) sliding (3, 3) map {
          case name :: "=" :: value :: Nil => (name, value)
          case _ => fail
        }).toSeq

        // perform the query
        select[S](fields: _*) from domainName where (condition: _*) map (_.bean)
      case _ => fail
    }
  }

  def parseQuery(query: String): List[String] = {
    val sb = new StringBuilder()
    var inDQ = false

    def newToken = { val tok = sb.toString(); sb.clear(); if (tok != "") Seq(tok) else Seq.empty }
    def delimToken(c: Char) = newToken ++ Seq(String.valueOf(c))
    def toggleDQuotes = { inDQ = !inDQ; Seq.empty }
    def append(c: Char) = { sb += c; Seq.empty }

    val results = (query flatMap {
      case '"' => toggleDQuotes
      case c if c.isSpaceChar || c == ',' => if (!inDQ) newToken else append(c)
      case c if !c.isLetterOrDigit => if (!inDQ) delimToken(c) else append(c)
      case c => append(c)
    }).toList

    if (sb.isEmpty) results else results ::: sb.toString :: Nil
  }

  /**
   * CLI Yahoo! Finance Profile Source
   * @author lawrence.daniels@gmail.com
   */
  case class CxYFBusinessProfileSource() extends AssetDataSource[CompleteQuote] {
    import YFBusinessProfileService._
    lazy val provides = extractFields(classOf[YFProfile])

    override def load(fields: Set[String], conditions: Seq[(String, Any)], cq: CompleteQuote)(implicit ec: ExecutionContext) = {
      for {
        p <- YFBusinessProfileService.getProfile(getCondition[String]("symbol", conditions))

        result = {
          ScalaBeanUtil.copy(p, cq)
          Seq(SuccessResult(this, p, p.responseTimeMsec))
        }
      } yield result
    }
  }

  /**
   * CLI Yahoo! Finance Currency Source
   * @author lawrence.daniels@gmail.com
   */
  case class CxYFCurrencySource() extends AssetDataSource[CurrencyQuote] {
    import YFCurrencyQuoteService._
    lazy val provides = extractFields(classOf[YFCurrencyQuote])

    override def load(fields: Set[String], conditions: Seq[(String, Any)], cq: CurrencyQuote)(implicit ec: ExecutionContext) = {
      for {
        q <- YFCurrencyQuoteService.getQuote(getCondition[String]("symbol", conditions))

        result = {
          ScalaBeanUtil.copy(q, cq)
          Seq(SuccessResult(this, q, q.responseTimeMsec))
        }
      } yield result
    }
  }

  /**
   * CLI Yahoo! Finance Real-time Stock Quote Service
   * @author lawrence.daniels@gmail.com
   */
  case class CxYFRealtimeStockQuoteSource() extends AssetDataSource[CompleteQuote] {
    import YFRealtimeStockQuoteService._
    lazy val provides = extractFields(classOf[YFRealtimeQuote])

    override def load(fields: Set[String], conditions: Seq[(String, Any)], cq: CompleteQuote)(implicit ec: ExecutionContext) = {
      for {
        q <- YFRealtimeStockQuoteService.getQuote(getCondition[String]("symbol", conditions))

        result = {
          ScalaBeanUtil.copy(q, cq)
          Seq(SuccessResult(this, q, q.responseTimeMsec))
        }
      } yield result
    }
  }

}

/**
 * Default Service Directory Singleton
 * @author lawrence.daniels@gmail.com
 */
object DefaultServiceDirectory {

  /**
   * Represents a stock/ETF quote
   * @author lawrence.daniels@gmail.com
   */
  class CompleteQuote() {
    import java.util.Date

    var symbol: String = _
    var name: Option[String] = _
    var exchange: Option[String] = _
    var lastTrade: Option[Double] = _
    var tradeTime: Option[Date] = _
    var chg: Option[Double] = _
    var chgPct: Option[Double] = _
    var prevClose: Option[Double] = _
    var open: Option[Double] = _
    var close: Option[Double] = _
    var ask: Option[Double] = _
    var askSize: Option[Int] = _
    var bid: Option[Double] = _
    var bidSize: Option[Int] = _
    var target1Yr: Option[Double] = _
    var beta: Option[Double] = _
    var nextEarningsDate: Option[Date] = _
    var daysLow: Option[Double] = _
    var daysHigh: Option[Double] = _
    var _52wLow: Option[Double] = _
    var _52wHigh: Option[Double] = _
    var volume: Option[Long] = _
    var avgVol3m: Option[Long] = _
    var marketCap: Option[Double] = _
    var peRatio: Option[Double] = _
    var eps: Option[Double] = _
    var dividend: Option[Double] = _
    var divYield: Option[Double] = _

    var indexMembership: Option[String] = _
    var sector: Option[String] = _
    var industry: Option[String] = _
    var fullTimeEmployees: Option[Int] = _
    var businessSummary: Option[String] = _
  }

  /**
   * Represents a currency quote
   * @author lawrence.daniels@gmail.com
   */
  class CurrencyQuote() {
    var symbol: String = _
    var prevClose: Option[Double] = _
    var open: Option[Double] = _
    var ask: Option[Double] = _
    var bid: Option[Double] = _
    var daysLow: Option[Double] = _
    var daysHigh: Option[Double] = _
    var _52wLow: Option[Double] = _
    var _52wHigh: Option[Double] = _
    var responseTimeMsec: Long = _
  }

} 