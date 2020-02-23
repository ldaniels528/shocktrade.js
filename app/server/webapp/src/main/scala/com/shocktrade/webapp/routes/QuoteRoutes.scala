package com.shocktrade.webapp.routes

import com.shocktrade.common.models.quote.DiscoverQuote._
import com.shocktrade.common.models.quote.{AutoCompleteQuote, DiscoverQuote, OrderQuote}
import com.shocktrade.server.dao.securities.KeyStatisticsDAO._
import com.shocktrade.server.dao.securities.NAICSDAO._
import com.shocktrade.server.dao.securities.SICDAO._
import com.shocktrade.server.dao.securities._
import com.shocktrade.server.services.yahoo.YahooFinanceCSVHistoryService
import com.shocktrade.webapp.routes.QuoteRoutes._
import com.shocktrade.webapp.routes.explore.StockQuoteDAO
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb._
import io.scalajs.util.DateHelper._
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Quote Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class QuoteRoutes(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext) {
  private val stockQuoteDAO = StockQuoteDAO()
  implicit val securitiesDAO: Future[SecuritiesDAO] = dbFuture.map(SecuritiesDAO.apply)
  implicit val keyStatisticsDAO: Future[KeyStatisticsDAO] = dbFuture.map(_.getKeyStatisticsDAO)
  implicit val naicsDAO: Future[NAICSDAO] = dbFuture.map(_.getNAICSDAO)
  implicit val sicDAO: Future[SICDAO] = dbFuture.map(_.getSICDAO)
  private val historySvc = new YahooFinanceCSVHistoryService()

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
      quote <- securitiesDAO.flatMap(_.findCompleteQuote(symbol))
      naics <- quote.flatMap(_.naicsNumber.toOption map (code => naicsDAO.flatMap(_.findByCode(code)))) getOrElse Future.successful(None)
      sic <- quote.flatMap(_.sicNumber.toOption map (code => sicDAO.flatMap(_.findByCode(code)))) getOrElse Future.successful(None)
      discoverQuote = quote.map(_.toDiscover).map { q =>
        q.naicsDescription = naics.orUndefined.flatMap(_.description)
        q.sicDescription = sic.orUndefined.flatMap(_.description)
        q
      }
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
    securitiesDAO.flatMap(_.findCompleteQuote(symbol)) onComplete {
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
    securitiesDAO.flatMap(_.findQuotesBySymbols[OrderQuote](symbols, fields = OrderQuote.Fields)) onComplete {
      case Success(quotes) => response.send(quotes); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def statistics(request: Request, response: Response, next: NextFunction): Unit = {
    val symbol = request.getSymbol
    keyStatisticsDAO.flatMap(_.findBySymbol(symbol)) onComplete {
      case Success(Some(keystats)) => response.send(keystats); next()
      case Success(None) => response.notFound(); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def tradingHistory(request: Request, response: Response, next: NextFunction): Unit = {
    val symbol = request.getSymbol
    historySvc(symbol, from = new js.Date() - 30.days, to = new js.Date()) onComplete {
      case Success(history) => response.send(history.quotes); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Searches symbols and company names for a match to the specified search term
   */
  def search(request: Request, response: Response, next: NextFunction): Unit = {
    request.query.get("searchTerm") match {
      case Some(searchTerm) =>
        val maxResults = request.getMaxResults(default = 20)
        securitiesDAO.flatMap(_.search(searchTerm, maxResults)) onComplete {
          case Success(results) => response.send(enrichSearchResults(results)); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      case None =>
        response.badRequest("Missing search term (searchTerm)"); next()
    }
  }

  private def copyValues(q: DiscoverQuote, ks: KeyStatisticsData): Unit = {
    q.avgVolume10Day = ks.averageVolume10days
    q.beta = ks.beta
    q.movingAverage200Day = ks.movingAverage200Day
    q.movingAverage50Day = ks.movingAverage50Day
    q.forwardPE = ks.forwardPE

    // TODO add the other fields
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
