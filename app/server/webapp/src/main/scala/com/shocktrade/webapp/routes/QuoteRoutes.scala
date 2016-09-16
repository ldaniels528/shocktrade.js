package com.shocktrade.webapp.routes

import com.shocktrade.common.dao.quotes.NAICSDAO._
import com.shocktrade.common.dao.quotes.SecuritiesDAO._
import com.shocktrade.common.dao.quotes.SICDAO._
import com.shocktrade.common.models.quote.DiscoverQuote._
import com.shocktrade.common.models.quote.{AutoCompleteQuote, ResearchQuote}
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Quote Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object QuoteRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB) = {
    implicit val securitiesDAO = dbFuture.flatMap(_.getSecuritiesDAO)
    implicit val naicsDAO = dbFuture.flatMap(_.getNAICSDAO)
    implicit val sicDAO = dbFuture.flatMap(_.getSICDAO)

    // collections of quotes
    app.get("/api/quotes/search", (request: Request, response: Response, next: NextFunction) => search(request, response, next))

    // individual quotes
    app.get("/api/quote/:symbol/basic", (request: Request, response: Response, next: NextFunction) => basicQuote(request, response, next))
    app.get("/api/quote/:symbol/discover", (request: Request, response: Response, next: NextFunction) => quoteBySymbol(request, response, next))
    app.get("/api/quote/:symbol/full", (request: Request, response: Response, next: NextFunction) => fullQuote(request, response, next))
    app.get("/api/quote/:symbol/history", (request: Request, response: Response, next: NextFunction) => tradingHistory(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def quoteBySymbol(request: Request, response: Response, next: NextFunction) = {
      val symbol = request.getSymbol
      val outcome = for {
        quote <- securitiesDAO.flatMap(_.findCompleteQuote(symbol))
        naics <- quote.flatMap(_.naicsNumber.toOption map (code => naicsDAO.flatMap(_.findByCode(code)))) getOrElse Future.successful(None)
        sic <- quote.flatMap(_.sicNumber.toOption map (code => sicDAO.flatMap(_.findByCode(code)))) getOrElse Future.successful(None)
        discoverQuote = quote.map(_.toDiscover).map { q =>
          q.getAdvisory foreach { advisory =>
            q.advisory = advisory.description
            q.advisoryType = advisory.`type`
          }
          q.naicsDescription = naics.orUndefined.flatMap(_.description)
          q.sicDescription = sic.orUndefined.flatMap(_.description)
          q.riskLevel = q.getRiskLevel
          q
        }
      } yield discoverQuote

      outcome onComplete {
        case Success(Some(quote)) => response.send(quote); next()
        case Success(None) => response.notFound(); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def basicQuote(request: Request, response: Response, next: NextFunction) = {
      val symbol = request.getSymbol
      securitiesDAO.flatMap(_.findQuote[ResearchQuote](symbol, fields = ResearchQuote.Fields)) onComplete {
        case Success(Some(quote)) => response.send(quote); next()
        case Success(None) => response.notFound(); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def fullQuote(request: Request, response: Response, next: NextFunction) = {
      val symbol = request.getSymbol
      securitiesDAO.flatMap(_.findCompleteQuote(symbol)) onComplete {
        case Success(Some(quote)) => response.send(quote); next()
        case Success(None) => response.notFound(); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def tradingHistory(request: Request, response: Response, next: NextFunction) = {
      val symbol = request.getSymbol
      response.send(doc())
      next()
    }

    /**
      * Searches symbols and company names for a match to the specified search term
      */
    def search(request: Request, response: Response, next: NextFunction) = {
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

    def enrichSearchResults(results: js.Array[AutoCompleteQuote]) = {
      results foreach { result =>
        result.icon = result.getIcon
      }
      results
    }

  }

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
