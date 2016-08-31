package com.shocktrade.javascript.routes

import com.shocktrade.javascript.data.NewsDAO
import com.shocktrade.javascript.data.NewsDAO._
import com.shocktrade.javascript.util.RSSFeedParser
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * News Routes
  * @author lawrence.daniels@gmail.com
  */
object NewsRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) = {
    implicit val newsDAO = dbFuture.flatMap(_.getNewsDAO)
    implicit val rss = new RSSFeedParser()

    app.get("/api/news/feed/:id", (request: Request, response: Response, next: NextFunction) => getFeedsByID(request, response, next))
    app.get("/api/news/source/:id", (request: Request, response: Response, next: NextFunction) => getSourceByID(request, response, next))
    app.get("/api/news/sources", (request: Request, response: Response, next: NextFunction) => getSources(request, response, next))
  }

  def getFeedsByID(request: Request, response: Response, next: NextFunction)(implicit newsDAO: Future[NewsDAO], mongo: MongoDB, rss: RSSFeedParser) = {
    val id = request.params("id")
    val outcome = for {
      sourceOpt <- newsDAO.flatMap(_.findByID(id))
      url = sourceOpt.map(_.url) getOrElse "http://rss.cnn.com/rss/money_markets.rss"
      feeds <- rss.parse(url)
    } yield feeds

    outcome onComplete {
      case Success(feeds) => response.send(feeds); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def getSourceByID(request: Request, response: Response, next: NextFunction)(implicit newsDAO: Future[NewsDAO], mongo: MongoDB) = {
    val id = request.params("id")
    newsDAO.flatMap(_.findByID(id)) onComplete {
      case Success(Some(source)) => response.send(source); next()
      case Success(None) => response.notFound(id); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def getSources(request: Request, response: Response, next: NextFunction)(implicit newsDAO: Future[NewsDAO]) = {
    newsDAO.flatMap(_.findSources) onComplete {
      case Success(sources) => response.send(sources); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

}
