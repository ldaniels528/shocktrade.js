package com.shocktrade.webapp.routes.discover

import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.routes.discover.dao.RSSFeedDAO
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
 * RSS Feed Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RSSFeedRoutes(app: Application)(implicit ec: ExecutionContext, rssFeedDAO: RSSFeedDAO) {
  implicit val rss: NewRSSFeedParser = new NewRSSFeedParser()

  app.get("/api/news/feed/:id", (request: Request, response: Response, next: NextFunction) => feedsByID(request, response, next))
  app.get("/api/news/source/:id", (request: Request, response: Response, next: NextFunction) => sourceByID(request, response, next))
  app.get("/api/news/sources", (request: Request, response: Response, next: NextFunction) => sources(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def feedsByID(request: Request, response: Response, next: NextFunction): Unit = {
    val id = request.params("id")
    val outcome = for {
      sourceOpt <- rssFeedDAO.findByID(id)
      url = sourceOpt.flatMap(_.url.toOption) getOrElse "http://rss.cnn.com/rss/money_markets.rss"
      feeds <- rss.parse(url).toFuture
    } yield feeds

    outcome onComplete {
      case Success(feeds) => response.send(feeds); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def sourceByID(request: Request, response: Response, next: NextFunction): Unit = {
    val id = request.params("id")
    rssFeedDAO.findByID(id) onComplete {
      case Success(Some(source)) => response.send(source); next()
      case Success(None) => response.notFound(id); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def sources(request: Request, response: Response, next: NextFunction): Unit = {
    rssFeedDAO.findRSSFeeds onComplete {
      case Success(sources) => response.send(sources); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

}
