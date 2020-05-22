package com.shocktrade.server.services

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.services.NASDAQCompanyListService._
import io.scalajs.npm.csvparse._
import io.scalajs.npm.request._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * NASDAQ Company List Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NASDAQCompanyListService() {
  private val logger = LoggerFactory.getLogger(getClass)

  def amex()(implicit ec: ExecutionContext): Future[Seq[NASDAQCompanyInfo]] = {
    download(exchange = "AMEX", url = "http://old.nasdaq.com/screening/companies-by-industry.aspx?exchange=AMEX&render=download")
  }

  def nasdaq()(implicit ec: ExecutionContext): Future[Seq[NASDAQCompanyInfo]] = {
    download(exchange = "NASDAQ", url = "http://old.nasdaq.com/screening/companies-by-industry.aspx?exchange=NASDAQ&render=download")
  }

  def nyse()(implicit ec: ExecutionContext): Future[Seq[NASDAQCompanyInfo]] = {
    download(exchange = "NYSE", url = "http://old.nasdaq.com/screening/companies-by-industry.aspx?exchange=NYSE&render=download")
  }

  private def download(exchange: String, url: String)(implicit ec: ExecutionContext): Future[Seq[NASDAQCompanyInfo]] = {
    Request.getFuture(url) flatMap {
      case (response, _) if response.statusCode != 200 => throw js.JavaScriptException(response.statusMessage)
      case (_, data) => parseCSVBlob(exchange, data)
    }
  }

}

/**
 * NASDAQ Company List Service Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object NASDAQCompanyListService {
  private val headers = Seq("Symbol", "Name", "LastSale", "MarketCap", "ADRTSO", "IPOyear", "Sector", "Industry", "Summary Quote")

  def parseCSVBlob(exchange: String, data: js.Any)(implicit ec: ExecutionContext): Future[Seq[NASDAQCompanyInfo]] = {
    val linesNoHeader = data.toString.split("[\n]").drop(1).mkString("\n")
    CsvParse.parseFuture(linesNoHeader, new ParserOptions()) map { rows =>
      rows.map(values => js.Dictionary(headers zip values.toSeq: _*)).toSeq.map(toCompanyInfo(exchange, _))
    }
  }

  def toCompanyInfo(exchange: String, mapping: js.Dictionary[String]): NASDAQCompanyInfo = {
    new NASDAQCompanyInfo(
      symbol = mapping.get("Symbol").flatMap(nullify).orUndefined,
      exchange = exchange,
      name = mapping.get("Name").flatMap(nullify).orUndefined,
      lastSale = mapping.get("LastSale").flatMap(nullify).map(_.toDouble).orUndefined,
      marketCap = mapping.get("MarketCap").flatMap(nullify).map(_.toDouble).orUndefined,
      ADRTSO = mapping.get("ADRTSO").flatMap(nullify).orUndefined,
      IPOyear = mapping.get("IPOyear").flatMap(nullify).map(_.toInt).orUndefined,
      sector = mapping.get("Sector").flatMap(nullify).orUndefined,
      industry = mapping.get("Industry").flatMap(nullify).orUndefined,
      summaryQuote = mapping.get("Summary Quote").flatMap(nullify).orUndefined)
  }

  @inline private def nullify(s: String): Option[String] = if (s.isEmpty || s == "n/a") None else Option(s)

  class NASDAQCompanyInfo(val symbol: js.UndefOr[String],
                          val exchange: js.UndefOr[String],
                          val name: js.UndefOr[String],
                          val lastSale: js.UndefOr[Double],
                          val marketCap: js.UndefOr[Double],
                          val ADRTSO: js.UndefOr[String],
                          val IPOyear: js.UndefOr[Int],
                          val sector: js.UndefOr[String],
                          val industry: js.UndefOr[String],
                          val summaryQuote: js.UndefOr[String]) extends js.Object

}
