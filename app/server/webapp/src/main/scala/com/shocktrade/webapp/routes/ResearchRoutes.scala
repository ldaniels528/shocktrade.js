package com.shocktrade.webapp.routes

import com.shocktrade.server.dao.securities.SecuritiesDAO._
import com.shocktrade.common.forms.ResearchOptions
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * Research Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ResearchRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB) = {
    implicit val quoteDAO = dbFuture.flatMap(_.getSecuritiesDAO)

    app.post("/api/research/search", (request: Request, response: Response, next: NextFunction) => search(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    /**
      * Searches symbols and company names for a match to the specified search term
      */
    def search(request: Request, response: Response, next: NextFunction) = {
      val options = request.bodyAs[ResearchOptions]
      quoteDAO.flatMap(_.research(options)) onComplete {
        case Success(results) => response.send(results); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

  }

}
