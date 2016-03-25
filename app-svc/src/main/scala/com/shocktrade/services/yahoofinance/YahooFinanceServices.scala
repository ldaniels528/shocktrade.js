package com.shocktrade.services.yahoofinance

import com.shocktrade.services.yahoofinance.YFStockQuoteService.YFStockQuote

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/**
 * Yahoo! Finance Services
 * @author lawrence.daniels@gmail.com
 */
object YahooFinanceServices {

  /**
   * Rewrites symbols for use with Yahoo! Finance Services
   */
  def fixSymbol(symbol: String) = {
    // does symbol contain a dot (.)?
    if (symbol.endsWith(".OB")) symbol.dropRight(3) else symbol
  }

  /**
   * Retrieves for a quote for the given currency symbol
   * @param symbol the given currency symbol (e.g. "EUR=X")
   * @return a [[Future]] of a [[YFCurrencyQuoteService.YFCurrencyQuote]]
   */
  def getCurrecncyQuote(symbol: String)(implicit ec: ExecutionContext) = YFCurrencyQuoteService.getQuote(symbol)

  /**
   * Retrieves historical quotes for the given symbol
   * @param symbol the given ticker (e.g. "AAPL")
   */
  def getHistoricalQuotes(symbol: String)(implicit ec: ExecutionContext) = YFHistoricalQuoteService.getQuotes(symbol)

  /**
   * Crawls the finance.yahoo.com to retrieve the key statistics for a stock symbol
   * @param symbol the given symbol (e.g., "AAPL")
   * @return a web service object containing the requested data
   */
  def getKeyStatistics(symbol: String)(implicit ec: ExecutionContext) = YFKeyStatisticsService.getKeyStatistics(symbol)

  /**
   * Retrieves an option quote for the given symbol
   * @param symbol the given symbol (e.g. "BRKB140118C00040000")
   * @return a [[Future]] of a [[YFOptionQuoteService.YFOptionQuote]]
   */
  def getOptionQuotes(symbol: String)(implicit ec: ExecutionContext) = YFOptionQuoteService.getQuotes(symbol)

  /**
   * Retrieves the ticker's profile (business or fund summary)
   */
  def getProfile(symbol: String)(implicit ec: ExecutionContext) = YFBusinessProfileService.getProfile(symbol)

  /**
   * Performs the service call and returns a single object read from the service.
   * @param symbol the given stock symbol (e.g., "AAPL")
   * @return the [[Future]] of symbols to [[YFRealtimeStockQuoteService.YFRealtimeQuote]]
   */
  def getRealTimeStockQuote(symbol: String)(implicit ec: ExecutionContext) = YFRealtimeStockQuoteService.getQuote(symbol)

  /**
   * Performs the service call and returns a single object read from the service.
   * @param symbol the given stock symbol (e.g., "AAPL")
   * @param params the given stock fields (e.g., "soxq2")
   * @return the [[YFStockQuote]]
   */
  def getStockQuote(symbol: String, params: String)(implicit ec: ExecutionContext) = YFStockQuoteService.getQuote(symbol, params)

  /**
   * Performs the service call and returns a single object read from the service.
   * @param symbols the given list of stock symbols (e.g., "AAPL+MSFT+INTC+AMD+GOOG+AMZN")
   * @param params the given stock fields (e.g., "soxq2")
   * @return the [[Map]] of symbols to a collection [[YFStockQuote]]
   */
  def getStockQuotes(symbols: Seq[String], params: String)(implicit ec: ExecutionContext) = YFStockQuoteService.getQuotes(symbols, params)

  /**
   * Performs the service call and returns a single object read from the service.
   * @param params the field inclusion parameters
   * @param beanClass the given bean [[Class]]
   * @param symbols the plus symbol (+) concatenated list of symbols (e.g., "AAPL+GOOG")
   * @return the [[Map]] of symbols to quotes
   */
  def getStockQuotes[T](symbols: Seq[String], params: String, beanClass: Class[T])(implicit ec: ExecutionContext): Future[Seq[T]] = {
    val results = for {
      // retrieve the quotes
      quotes <- getStockQuotes(symbols, params)

      // retrieve any changed symbol quotes
      changed <- getChangedSymbolQuotes(quotes, params)
    } yield (quotes, changed)

    val promise = Promise[Seq[T]]()
    results.onComplete {
      case Success((quotes, changed)) =>
        // build sequence of typed objects with the most up-to-date quotes
        val typedQuotes = quotes map (q => (q.symbol, q)) map {
          case (symbol, q) => toStockQuote(changed.getOrElse(symbol, q), beanClass)
        }
        promise.success(typedQuotes)
      case Failure(e) => promise.failure(e)
    }
    promise.future
  }

  /**
   * This service attempt to find the symbol for the specified company name
   * @param query the specified query (e.g., "Paragon")
   * @return the list of search results
   */
  def getSymbolSuggestions(query: String)(implicit ec: ExecutionContext) = YFSymbolSuggestionService.search(query)

  /**
   * Returns the quotes for changed symbols
   */
  def getChangedSymbolQuotes(quotes: Seq[YFStockQuote], params: String)(implicit ec: ExecutionContext): Future[Map[String, YFStockQuote]] = {
    // determine if any tickers have changed
    val oldToNewSeq = Map(quotes map (q => (q.symbol, q.newSymbol)): _*)

    // get just the sequence of changed symbols
    val newSymbols = oldToNewSeq.toSeq flatMap { case (oldSymbol, newSymbol) => newSymbol } 

    // create a new promise
    val promise = Promise[Map[String, YFStockQuote]]()

    // get the updated quotes (changed symbols only)
    val task = getStockQuotes(newSymbols, params)
    task.onComplete {
      case Failure(e) => promise.failure(e)
      case Success(updatedQuotes) =>
        // create a mapping of the updated quotes
        val updatedQuoteMap = Map(updatedQuotes map (q => (q.symbol, q)): _*)

        // build the old symbol to new quote mapping
        val oldSymbolToNewQuotes = oldToNewSeq flatMap {
          case (oldSymbol, newSymbol) =>
            newSymbol match {
              case Some(symbol) => Some((oldSymbol, updatedQuoteMap(symbol)))
              case None => None
            }
        }
        promise.success(oldSymbolToNewQuotes)
    }
    promise.future
  }

  /**
   * Returns a quote of the specified type
   * @param quote the given Yahoo! Finance quote
   * @param beanClass the given bean [[Class]]
   * @return a quote of the specified type
   */
  def toStockQuote[T](quote: YFStockQuote, beanClass: Class[T]): T = {
    import org.apache.commons.beanutils.BeanUtils

    // create the quote
    val typedQuote = beanClass.newInstance()

    // copy the Yahoo quote data
    BeanUtils.copyProperties(quote, typedQuote)
    typedQuote
  }

}