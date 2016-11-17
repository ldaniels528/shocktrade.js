package com.shocktrade.server.facade

import com.shocktrade.server.dao.securities.SecuritiesDAO._
import com.shocktrade.server.facade.PricingFacade._
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService.YFCSVQuote
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.mongodb.{Db, MongoDB}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Pricing Facade
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class PricingFacade(dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire) {
  // load the modules
  private implicit val mongo = MongoDB()

  // get DAO references
  private val securitiesDAO = dbFuture.flatMap(_.getSecuritiesDAO)

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
  def findQuotes(symbols: Seq[String]) = {
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
  def fromDatabase(symbols: Seq[String]) = {
    securitiesDAO.flatMap(_.findQuotesBySymbols[PricingQuote](symbols, fields = PricingQuote.Fields))
  }

  /**
    * Retrieves pricing quotes from the service layer
    * @param symbols the given symbols
    * @return the collection of [[PricingQuote pricing quotes]]
    */
  def fromServices(symbols: Seq[String]) = {
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