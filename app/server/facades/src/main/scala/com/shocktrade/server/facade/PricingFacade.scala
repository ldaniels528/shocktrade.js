package com.shocktrade.server.facade

import com.shocktrade.server.dao.securities.SecuritiesDAO
import com.shocktrade.server.facade.PricingFacade._
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService.YFCSVQuote
import io.scalajs.npm.mongodb.Db

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Pricing Facade
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class PricingFacade(dbFuture: Future[Db])(implicit ec: ExecutionContext) {

  // get DAO references
  private val securitiesDAO = dbFuture.map(SecuritiesDAO.apply)

  // get service references
  private val csvQuoteSvc = new YahooFinanceCSVQuotesService()
  private val pricingQuoteParams = csvQuoteSvc.getParams(
    "symbol", "exchange", "lastTrade", "errorMessage"
  )

  /**
    * Retrieves pricing quotes from either the persistence- or service layer
    * @param symbols the given symbols
    * @return the collection of [[PricingQuote pricing quotes]]
    */
  def findQuotes(symbols: Seq[String]): Future[js.Array[PricingQuote]] = {
    for {
      foundQuotes <- fromDatabase(symbols)
      foundSymbols = foundQuotes.map(_.symbol).toSet
      missingSymbols = symbols.toSet.diff(foundSymbols)
      missingQuotes <- fromServices(missingSymbols.toSeq)
    } yield foundQuotes ++ missingQuotes
  }

  /**
    * Retrieves pricing quotes from the persistence layer
    * @param symbols the given symbols
    * @return the collection of [[PricingQuote pricing quotes]]
    */
  def fromDatabase(symbols: Seq[String]): Future[js.Array[PricingQuote]] = {
    securitiesDAO.flatMap(_.findQuotesBySymbols[PricingQuote](symbols, fields = PricingQuote.Fields))
  }

  /**
    * Retrieves pricing quotes from the service layer
    * @param symbols the given symbols
    * @return the collection of [[PricingQuote pricing quotes]]
    */
  def fromServices(symbols: Seq[String]): Future[Seq[PricingQuote]] = {
    csvQuoteSvc.getQuotes(pricingQuoteParams, symbols: _*) map (_ map (_.toPricingQuote))
  }

}

/**
  * Pricing Facade Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PricingFacade {

  /**
    * Pricing Quote Extensions
    * @param csvQuote the given [[YFCSVQuote CSV quote]]
    */
  implicit class PricingQuoteExtensions(val csvQuote: YFCSVQuote) extends AnyVal {

    @inline
    def toPricingQuote = new PricingQuote(
      symbol = csvQuote.symbol,
      lastTrade = csvQuote.lastTrade,
      tradeDateTime = csvQuote.tradeDateTime
    )

  }

}