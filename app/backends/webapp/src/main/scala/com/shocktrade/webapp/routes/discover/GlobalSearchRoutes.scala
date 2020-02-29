package com.shocktrade.webapp.routes.discover

import com.shocktrade.common.forms.MaxResultsForm
import com.shocktrade.webapp.routes.NextFunction
import com.shocktrade.webapp.routes.discover.GlobalSearchRoutes._
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Global Search Routes
 * @author lawrence.daniels@gmail.com
 */
class GlobalSearchRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val globalSearchDAO = GlobalSearchDAO()

  app.get("/api/search", (request: Request, response: Response, next: NextFunction) => searchResults(request, response, next))

  /**
   * Searches for people, groups, organizations and events
   * @example GET /api/search?searchTerm=mic&maxResults=10
   */
  def searchResults(request: Request, response: Response, next: NextFunction): Unit = {
    val form = request.queryAs[SearchForm]
    form.searchTerm.toOption match {
      case Some(searchTerm) =>
        globalSearchDAO.search(searchTerm, maxResults = form.getMaxResults()) onComplete {
          case Success(results) =>
            response.send(results); next()
          case Failure(e) =>
            response.internalServerError(e.getMessage)
            e.printStackTrace()
        }
      case None =>
        response.badRequest(form); next()
    }
  }

}

/**
 * Search Routes Companion
 * @author lawrence.daniels@gmail.com
 */
object GlobalSearchRoutes {

  /**
   * Search Form
   * @author lawrence.daniels@gmail.com
   */
  @js.native
  trait SearchForm extends MaxResultsForm {
    var searchTerm: js.UndefOr[String] = js.native
  }

}
