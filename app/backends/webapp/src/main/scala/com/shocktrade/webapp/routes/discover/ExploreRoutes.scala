package com.shocktrade.webapp.routes.discover

import com.shocktrade.webapp.routes.NextFunction
import com.shocktrade.webapp.routes.discover.dao._
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
 * Explore Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ExploreRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val stockQuoteDAO = StockQuoteDAO()
  private val exploreDAO = ExploreDAO()

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
        exploreDAO.exploreIndustries(sector) onComplete {
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
          case Some(subIndustry) => exploreDAO.findQuotesBySubIndustry(sector, industry, subIndustry)
          case None => exploreDAO.findQuotesByIndustry(sector, industry)
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
    val symbol = request.params("symbol")
    stockQuoteDAO.findQuote(symbol) onComplete {
      case Success(Some(quote)) => response.send(quote); next()
      case Success(None) => response.notFound(symbol); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def sectors(request: Request, response: Response, next: NextFunction): Unit = {
    exploreDAO.exploreSectors onComplete {
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
        exploreDAO.exploreSubIndustries(sector, industry) onComplete {
          case Success(results) => response.send(results); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      case None =>
        response.badRequest("One or more required parameters (sector, industry) is missing"); next()
    }
  }

}
