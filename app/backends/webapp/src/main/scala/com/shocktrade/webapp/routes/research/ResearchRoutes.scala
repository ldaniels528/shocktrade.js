package com.shocktrade.webapp.routes
package research

import com.shocktrade.common.api.ResearchAPI
import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.webapp.routes.research.dao.ResearchDAO
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Research Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ResearchRoutes(app: Application)(implicit ec: ExecutionContext, researchDAO: ResearchDAO) extends ResearchAPI {
  // API routes
  app.get(researchURL(), (request: Request, response: Response, next: NextFunction) => research(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Searches symbols and company names for a match to the specified search term
   */
  def research(request: Request, response: Response, next: NextFunction): Unit = {
    val nullFilter = (s: String) => if (s == "" || s == "null") None else Some(s)
    val options = new ResearchOptions(
      changeMax = request.query.get("changeMax").flatMap(nullFilter).map(_.toDouble).orUndefined,
      changeMin = request.query.get("changeMin").flatMap(nullFilter).map(_.toDouble).orUndefined,
      priceMax = request.query.get("priceMax").flatMap(nullFilter).map(_.toDouble).orUndefined,
      priceMin = request.query.get("priceMin").flatMap(nullFilter).map(_.toDouble).orUndefined,
      spreadMax = request.query.get("spreadMax").flatMap(nullFilter).map(_.toDouble).orUndefined,
      spreadMin = request.query.get("spreadMin").flatMap(nullFilter).map(_.toDouble).orUndefined,
      volumeMax = request.query.get("volumeMax").flatMap(nullFilter).map(_.toDouble).orUndefined,
      volumeMin = request.query.get("volumeMin").flatMap(nullFilter).map(_.toDouble).orUndefined,
      sortBy = request.query.get("sortBy").flatMap(nullFilter).orUndefined,
      reverse = request.query.get("reverse").flatMap(nullFilter).map(_.toBoolean).orUndefined,
      maxResults = request.query.get("maxResults").flatMap(nullFilter).map(_.toInt).orUndefined
    )

    researchDAO.research(options) onComplete {
      case Success(results) => response.send(results); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

}
