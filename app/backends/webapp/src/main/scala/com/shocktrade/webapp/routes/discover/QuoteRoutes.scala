package com.shocktrade.webapp.routes
package discover

import com.shocktrade.common.models.quote.DiscoverQuote._
import com.shocktrade.common.models.quote.{AutoCompleteQuote, CompleteQuote, OrderQuote}
import com.shocktrade.webapp.routes.discover.QuoteRoutes._
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Quote Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class QuoteRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val autoCompleteDAO = AutoCompleteDAO()
  private val stockQuoteDAO = StockQuoteDAO()

  // collections of quotes
  app.post("/api/quotes/list", (request: Request, response: Response, next: NextFunction) => quotesList(request, response, next))
  app.get("/api/quotes/search", (request: Request, response: Response, next: NextFunction) => search(request, response, next))

  // individual quotes
  app.get("/api/quote/:symbol/basic", (request: Request, response: Response, next: NextFunction) => basicQuote(request, response, next))
  app.get("/api/quote/:symbol/discover", (request: Request, response: Response, next: NextFunction) => quoteBySymbol(request, response, next))
  app.get("/api/quote/:symbol/full", (request: Request, response: Response, next: NextFunction) => fullQuote(request, response, next))
  app.get("/api/quote/:symbol/history", (request: Request, response: Response, next: NextFunction) => tradingHistory(request, response, next))
  app.get("/api/quote/:symbol/order", (request: Request, response: Response, next: NextFunction) => orderQuote(request, response, next))
  app.get("/api/quote/:symbol/statistics", (request: Request, response: Response, next: NextFunction) => statistics(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def quoteBySymbol(request: Request, response: Response, next: NextFunction): Unit = {
    val symbol = request.getSymbol
    val outcome = for {
      quote <- stockQuoteDAO.findQuote[CompleteQuote](symbol)
      discoverQuote = quote.map(_.toDiscover)
    } yield discoverQuote

    outcome onComplete {
      case Success(Some(quote)) => response.send(quote); next()
      case Success(None) => response.notFound(); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def basicQuote(request: Request, response: Response, next: NextFunction): Unit = {
    val symbol = request.getSymbol
    stockQuoteDAO.findQuote(symbol) onComplete {
      case Success(Some(quote)) => response.send(quote); next()
      case Success(None) => response.notFound(); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def fullQuote(request: Request, response: Response, next: NextFunction): Unit = {
    val symbol = request.getSymbol
    stockQuoteDAO.findQuote(symbol) onComplete {
      case Success(Some(quote)) => response.send(quote); next()
      case Success(None) => response.notFound(); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def orderQuote(request: Request, response: Response, next: NextFunction): Unit = {
    val symbol = request.getSymbol
    stockQuoteDAO.findQuote(symbol) onComplete {
      case Success(Some(quote)) => response.send(quote); next()
      case Success(None) => response.notFound(); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def quotesList(request: Request, response: Response, next: NextFunction): Unit = {
    val symbols = request.bodyAs[js.Array[String]]
    stockQuoteDAO.findQuotes[OrderQuote](symbols) onComplete {
      case Success(quotes) => response.send(quotes); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def statistics(request: Request, response: Response, next: NextFunction): Unit = {
    val symbol = request.getSymbol
    /*
    keyStatisticsDAO.flatMap(_.findBySymbol(symbol)) onComplete {
      case Success(Some(keystats)) => response.send(keystats); next()
      case Success(None) => response.notFound(); next()
      case Failure(e) => response.internalServerError(e); next()
    }*/
    response.internalServerError("Not implemented");
    next()
  }

  def tradingHistory(request: Request, response: Response, next: NextFunction): Unit = {
    val symbol = request.getSymbol
    response.send(new js.Object())
  }

  /**
   * Searches symbols and company names for a match to the specified search term
   */
  def search(request: Request, response: Response, next: NextFunction): Unit = {
    request.query.get("searchTerm") match {
      case Some(searchTerm) =>
        val maxResults = request.getMaxResults()
        autoCompleteDAO.search(searchTerm, maxResults) onComplete {
          case Success(results) => response.send(enrichSearchResults(results)); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      case None =>
        response.badRequest("Missing search term (searchTerm)"); next()
    }
  }

  private def enrichSearchResults(results: js.Array[AutoCompleteQuote]): js.Array[AutoCompleteQuote] = {
    results foreach { result =>
      result.icon = result.getIcon
    }
    results
  }

}

/**
 * Quote Routes Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object QuoteRoutes {

  /**
   * Auto-Complete Quote Extensions
   * @param quote the given [[AutoCompleteQuote quote]]
   */
  implicit class AutoCompleteQuoteExtensions(val quote: AutoCompleteQuote) extends AnyVal {

    @inline
    def getIcon: js.UndefOr[String] = {
      (quote.assetType map {
        case "Crypto-Currency" => "fa fa-bitcoin st_blue"
        case "Currency" => "fa fa-dollar st_blue"
        case "ETF" => "fa fa-stack-exchange st_blue"
        case _ => "fa fa-globe st_blue"
      }) ?? "fa fa-globe st_blue"
    }

  }

}
