package com.shocktrade.webapp.routes

import com.shocktrade.server.dao.NewsDAO._
import com.shocktrade.server.services.RSSFeedParser

import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.{Db, MongoDB}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * News Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object NewsRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext) = {
    implicit val newsDAO = dbFuture.map(_.getNewsDAO)
    implicit val rss = new RSSFeedParser()

    app.get("/api/news/feed/:id", (request: Request, response: Response, next: NextFunction) => feedsByID(request, response, next))
    app.get("/api/news/source/:id", (request: Request, response: Response, next: NextFunction) => sourceByID(request, response, next))
    app.get("/api/news/sources", (request: Request, response: Response, next: NextFunction) => sources(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def feedsByID(request: Request, response: Response, next: NextFunction) = {
      val id = request.params.apply("id")
      val outcome = for {
        sourceOpt <- newsDAO.flatMap(_.findByID(id))
        url = sourceOpt.flatMap(_.url.toOption) getOrElse "http://rss.cnn.com/rss/money_markets.rss"
        feeds <- rss.parse(url)
      } yield feeds

      outcome onComplete {
        case Success(feeds) => response.send(feeds); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def sourceByID(request: Request, response: Response, next: NextFunction) = {
      val id = request.params.apply("id")
      newsDAO.flatMap(_.findByID(id)) onComplete {
        case Success(Some(source)) => response.send(source); next()
        case Success(None) => response.notFound(id); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def sources(request: Request, response: Response, next: NextFunction) = {
      newsDAO.flatMap(_.findSources) onComplete {
        case Success(sources) => response.send(sources); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

  }

}
