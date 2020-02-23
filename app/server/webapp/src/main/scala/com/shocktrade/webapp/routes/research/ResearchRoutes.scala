package com.shocktrade.webapp.routes
package research

import com.shocktrade.common.forms.ResearchOptions
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Research Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ResearchRoutes(app: Application)(implicit ec: ExecutionContext) {
  private implicit val researchDAO: ResearchDAO = ResearchDAO()

  app.post("/api/research/search", (request: Request, response: Response, next: NextFunction) => search(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Searches symbols and company names for a match to the specified search term
   */
  def search(request: Request, response: Response, next: NextFunction): Unit = {
    val options = request.bodyAs[ResearchOptions]
    researchDAO.research(options) onComplete {
      case Success(results) => response.send(results); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

}
