package com.shocktrade.javascript.routes

import com.shocktrade.javascript.data.StockQuoteDAO
import com.shocktrade.javascript.data.StockQuoteDAO._
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * Explore Routes
  * @author lawrence.daniels@gmail.com
  */
object ExploreRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB) = {
    implicit val quoteDAO = dbFuture.flatMap(_.getQuoteDAO)

    app.get("/api/explore/industries", (request: Request, response: Response, next: NextFunction) => industries(request, response, next))
    app.get("/api/explore/quotes", (request: Request, response: Response, next: NextFunction) => quotes(request, response, next))
    app.get("/api/explore/sectors", (request: Request, response: Response, next: NextFunction) => sectors(request, response, next))
    app.get("/api/explore/subIndustries", (request: Request, response: Response, next: NextFunction) => subIndustries(request, response, next))
    app.get("/api/explore/symbol/:symbol", (request: Request, response: Response, next: NextFunction) => sectorInfo(request, response, next))
  }

  def industries(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, quoteDAO: Future[StockQuoteDAO]) = {
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

  def sectorInfo(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, quoteDAO: Future[StockQuoteDAO]) = {
    val symbol = request.params("symbol")
    quoteDAO.flatMap(_.findSectorInfo(symbol)) onComplete {
      case Success(Some(quote)) => response.send(quote); next()
      case Success(None) => response.notFound(symbol); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def sectors(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, quoteDAO: Future[StockQuoteDAO]) = {
    quoteDAO.flatMap(_.exploreSectors) onComplete {
      case Success(results) => response.send(results); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def subIndustries(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, quoteDAO: Future[StockQuoteDAO]) = {
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

  def quotes(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, quoteDAO: Future[StockQuoteDAO]) = {
    val result = for {
      sector <- request.query.get("sector")
      industry <- request.query.get("industry")
      subIndustry <- request.query.get("subIndustry")
    } yield (sector, industry, subIndustry)

    result match {
      case Some((sector, industry, subIndustry)) =>
        quoteDAO.flatMap(_.findQuotesByIndustry(sector, industry, subIndustry)) onComplete {
          case Success(quotes) => response.send(quotes); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      case None =>
        response.badRequest("One or more required parameters (sector, industry, subIndustry) is missing"); next()
    }
  }

}
