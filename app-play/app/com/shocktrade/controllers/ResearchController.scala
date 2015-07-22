package com.shocktrade.controllers

import com.shocktrade.controllers.QuotesController._
import com.shocktrade.models.quote.{QuoteFilter, ResearchQuote}
import com.shocktrade.util.BSONHelper._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument => BS}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * Research Controller
 * @author lawrence.daniels@gmail.com
 */
object ResearchController extends Controller {
  private lazy val mcQ = db.collection[BSONCollection]("Stocks")
  private val MaxResults = 250

  ////////////////////////////////////////////////////////////////////////////
  //      API Functions
  ////////////////////////////////////////////////////////////////////////////

  def quoteSearch = Action.async { implicit request =>
    Try(request.body.asJson.map(_.as[QuoteFilter])) match {
      case Success(Some(form)) =>
        val maxResults = form.maxResults.map(Math.min(_, MaxResults)).getOrElse(MaxResults)
        mcQ.find(form.makeQuery, ResearchQuote.Fields.toBsonFields)
          .cursor[ResearchQuote]()
          .collect[Seq](maxResults) map (quotes => Ok(Json.toJson(quotes)))
      case Success(None) =>
        Future.successful(BadRequest("Proper JSON body expected"))
      case Failure(e) =>
        Logger.error(s"quoteSearch: json = ${request.body.asJson.orNull}", e)
        Future.successful(InternalServerError(e.getMessage))
    }
  }

}
