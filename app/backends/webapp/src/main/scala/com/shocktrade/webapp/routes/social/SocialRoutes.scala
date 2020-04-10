package com.shocktrade.webapp.routes
package social

import com.shocktrade.webapp.routes.social.PostRoutes.SharedContentForm
import com.shocktrade.webapp.{SharedContentParser, SharedContentProcessor}
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Social Routes
 * @author lawrence.daniels@gmail.com
 */
class SocialRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val seoMetaParser = new SharedContentParser()

  // Social Content / SEO
  app.get("/api/social/content", (request: Request, response: Response, next: NextFunction) => findSEOContent(request, response, next))

  /**
   * Retrieves the SEO information for embedding within a post
   * @example /api/social/content?url=http://www.businessinsider.com/how-to-remember-everything-you-learn-2015-10
   */
  def findSEOContent(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.queryAs[SharedContentForm]
    form.url.toOption match {
      case Some(url) =>
        seoMetaParser.parse(url) onComplete {
          case Success(result) =>
            SharedContentProcessor.parseMetaData(result) match {
              case Some(content) => response.send(content.toJson)
              case None => response.send(js.Dictionary[js.Any]())
            }
            next()
          case Failure(e) => response.showException(e).internalServerError(e); next()
        }
      case None => response.missingParams("url"); next()
    }
  }

}
