package com.shocktrade.javascript.routes

import com.shocktrade.javascript.data.StockQuoteDAO
import com.shocktrade.javascript.data.StockQuoteDAO._
import com.shocktrade.javascript.forms.ResearchSearchOptions
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * Research Routes
  * @author lawrence.daniels@gmail.com
  */
object ResearchRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB) = {
    implicit val quoteDAO = dbFuture.flatMap(_.getQuoteDAO)

    app.post("/api/research/search", (request: Request, response: Response, next: NextFunction) => search(request, response, next))
  }

  /**
    * Searches symbols and company names for a match to the specified search term
    */
  def search(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, quoteDAO: Future[StockQuoteDAO]) = {
    val options = request.bodyAs[ResearchSearchOptions]
    quoteDAO.flatMap(_.research(options)) onComplete {
      case Success(results) => response.send(results); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

}
