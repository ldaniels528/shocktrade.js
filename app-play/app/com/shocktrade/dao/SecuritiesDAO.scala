package com.shocktrade.dao

import com.shocktrade.models.quote.{AutoCompleteQuote, SectorQuote, _}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument => BS}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Securities DAO
  * @author lawrence.daniels@gmail.com
  */
trait SecuritiesDAO {

  /**
    * Auto-completes symbols and/or company names
    * @param searchTerm the given search term
    * @param maxResults the given maximum number of results
    * @return the promise of a collection of [[AutoCompleteQuote search results]]
    */
  def autoComplete(searchTerm: String, maxResults: Int = 20)(implicit ec: ExecutionContext): Future[Seq[AutoCompleteQuote]]

  def findBasicQuotes(symbols: Seq[String])(implicit ec: ExecutionContext): Future[Seq[BasicQuote]]

  def findByFilter(form: QuoteFilter, fields: Seq[String], maxResults: Int)(implicit ec: ExecutionContext): Future[Seq[ResearchQuote]]

  /**
    * Retrieves the exchange summary
    * @param ec the given [[ExecutionContext execution context]]
    * @return the given [[ExchangeSummary exchange summary]]
    */
  def findExchangeSummaries(implicit ec: ExecutionContext): Future[Seq[ExchangeSummary]]

  /**
    * Retrieves a complete quote; the composition of real-time quote and a disc-based quote
    * @param symbol the given ticker symbol
    * @param ec     the given [[ExecutionContext execution context]]
    * @return the [[Future promise]] of an option of a [[reactivemongo.bson.BSONDocument quote]]
    */
  def findFullQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[BS]]

  def findQuotesBySymbols(symbols: Seq[String])(implicit ec: ExecutionContext): Future[Seq[SectorQuote]]

  /**
    * Retrieves the NAICS code for the given NAICS number
    * @param naicsNumber the given NAICS number
    */
  def findNaicsCodeByNumber(naicsNumber: Int)(implicit ec: ExecutionContext): Future[Option[NaicsCode]]

  /**
    * Retrieves the NAICS code for the given NAICS numbers
    * @param naicsNumbers the given collection of NAICS numbers
    */
  def findNaicsCodeByNumbers(naicsNumbers: Seq[Int])(implicit ec: ExecutionContext): Future[Seq[NaicsCode]]

  def findProductQuotes(symbols: Seq[String])(implicit ec: ExecutionContext): Future[Seq[ProductQuote]]

  /**
    * Retrieves pricing for a collection of symbols
    */
  def findSnapshotQuotes(symbols: Seq[String])(implicit ec: ExecutionContext): Future[Seq[QuoteSnapshot]]

  /**
    * Retrieves sector information for the given symbol
    * @param symbol the given symbol (e.g. "AAPL")
    * @return the promise of an option of a [[SectorQuote sector quote]]
    */
  def findSectorQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[SectorQuote]]

  /**
    * Retrieves the SIC code for the given SIC number
    * @param sicNumber the given SIC number
    */
  def findSicCodeByNumber(sicNumber: Int)(implicit ec: ExecutionContext): Future[Option[SicCode]]

  /**
    * Retrieves the SIC code for the given SIC numbers
    * @param sicNumbers the given collection SIC numbers
    */
  def findSicCodeByNumbers(sicNumbers: Seq[Int])(implicit ec: ExecutionContext): Future[Seq[SicCode]]

}

/**
  * Securities DAO Companion Object
  * @author lawrence.daniels@gmail.com
  */
object SecuritiesDAO {

  def apply() = {
    new SecuritiesDAOSQLServer()
  }

  def apply(reactiveMongoApi: ReactiveMongoApi) = {
    new SecuritiesDAOMongoDB(reactiveMongoApi)
  }

}