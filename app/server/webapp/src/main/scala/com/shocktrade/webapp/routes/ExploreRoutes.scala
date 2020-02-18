package com.shocktrade.webapp.routes

import com.shocktrade.common.models.quote.SectorInfoQuote
import com.shocktrade.server.dao.securities.SecuritiesDAO
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * Explore Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ExploreRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext): Unit = {
    implicit val quoteDAO: Future[SecuritiesDAO] = dbFuture.map(SecuritiesDAO.apply)

    app.get("/api/explore/industries", (request: Request, response: Response, next: NextFunction) => industries(request, response, next))
    app.get("/api/explore/quotes", (request: Request, response: Response, next: NextFunction) => industryQuotes(request, response, next))
    app.get("/api/explore/sectors", (request: Request, response: Response, next: NextFunction) => sectors(request, response, next))
    app.get("/api/explore/subIndustries", (request: Request, response: Response, next: NextFunction) => subIndustries(request, response, next))
    app.get("/api/explore/symbol/:symbol", (request: Request, response: Response, next: NextFunction) => sectorInfo(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def industries(request: Request, response: Response, next: NextFunction): Unit = {
      request.query.get("sector") match {
        case Some(sector) =>
          quoteDAO.flatMap(_.exploreIndustries(sector)) onComplete {
            case Success(results) => response.send(results); next()
            case Failure(e) => response.internalServerError(e); next()
          }
        case None =>
          response.badRequest("One or more required parameters (sector) is missing"); next()
      }
    }

    def industryQuotes(request: Request, response: Response, next: NextFunction): Unit = {
      val result = for {
        sector <- request.query.get("sector")
        industry <- request.query.get("industry")
        subIndustry_? = request.query.get("subIndustry")
      } yield (sector, industry, subIndustry_?)

      result match {
        case Some((sector, industry, subIndustry_?)) =>
          val outcome = subIndustry_? match {
            case Some(subIndustry) => quoteDAO.flatMap(_.findQuotesBySubIndustry(sector, industry, subIndustry))
            case None => quoteDAO.flatMap(_.findQuotesByIndustry(sector, industry))
          }

          outcome onComplete {
            case Success(quotes) => response.send(quotes); next()
            case Failure(e) => response.internalServerError(e); next()
          }
        case None =>
          response.badRequest("One or more required parameters (sector, industry) is missing"); next()
      }
    }

    def sectorInfo(request: Request, response: Response, next: NextFunction): Unit = {
      val symbol = request.params.apply("symbol")
      quoteDAO.flatMap(_.findQuote[SectorInfoQuote](symbol, fields = SectorInfoQuote.Fields)) onComplete {
        case Success(Some(quote)) => response.send(quote); next()
        case Success(None) => response.notFound(symbol); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def sectors(request: Request, response: Response, next: NextFunction): Unit = {
      quoteDAO.flatMap(_.exploreSectors) onComplete {
        case Success(results) => response.send(results); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def subIndustries(request: Request, response: Response, next: NextFunction): Unit = {
      val result = for {
        sector <- request.query.get("sector")
        industry <- request.query.get("industry")
      } yield (sector, industry)

      result match {
        case Some((sector, industry)) =>
          quoteDAO.flatMap(_.exploreSubIndustries(sector, industry)) onComplete {
            case Success(results) => response.send(results); next()
            case Failure(e) => response.internalServerError(e); next()
          }
        case None =>
          response.badRequest("One or more required parameters (sector, industry) is missing"); next()
      }
    }

  }

}
