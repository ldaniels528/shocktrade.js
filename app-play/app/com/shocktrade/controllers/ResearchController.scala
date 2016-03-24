package com.shocktrade.controllers

import javax.inject.Inject

import com.shocktrade.dao.SecuritiesDAO
import com.shocktrade.models.quote.{QuoteFilter, ResearchQuote}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.Action
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Research Controller
  * @author lawrence.daniels@gmail.com
  */
class ResearchController @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends MongoController with ReactiveMongoComponents {
  private val securitiesDAO = SecuritiesDAO(reactiveMongoApi)
  private val MaxResults = 250

  ////////////////////////////////////////////////////////////////////////////
  //      API Functions
  ////////////////////////////////////////////////////////////////////////////

  def quoteSearch = Action.async { implicit request =>
    Try(request.body.asJson.map(_.as[QuoteFilter])) match {
      case Success(Some(form)) =>
        val maxResults = form.maxResults.map(Math.min(_, MaxResults)).getOrElse(MaxResults)
        securitiesDAO.findByFilter(form, ResearchQuote.Fields, maxResults) map (quotes => Ok(Json.toJson(quotes)))
      case Success(None) =>
        Future.successful(BadRequest("Proper JSON body expected"))
      case Failure(e) =>
        Logger.error(s"quoteSearch: json = ${request.body.asJson.orNull}", e)
        Future.successful(InternalServerError(e.getMessage))
    }
  }

}
