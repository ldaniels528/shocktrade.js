package com.shocktrade.controllers

import com.shocktrade.controllers.QuoteController._
import com.shocktrade.models.quote.QuoteFilter
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * Research Controller
 * @author lawrence.daniels@gmail.com
 */
object ResearchController extends Controller {
  private lazy val mcQ = db.collection[JSONCollection]("Stocks")
  private val fields = JS(
    "symbol" -> 1, "exchange" -> 1, "lastTrade" -> 1, "prevClose" -> 1, "open" -> 1,
    "changePct" -> 1, "low" -> 1, "high" -> 1, "spread" -> 1, "volume" -> 1
  )

  def quoteSearch = Action.async { implicit request =>
    Try(request.body.asJson.map(_.as[QuoteFilter])) match {
      case Success(Some(form)) =>
        val maxResults = form.maxResults.map(r => if (r <= 250) r else 250).getOrElse(250)
        mcQ.find(form.makeQuery, fields)
          .cursor[JsObject].collect[Seq](maxResults) map (js => Ok(Json.toJson(js)))
      case Success(None) =>
        Future.successful(BadRequest("Proper JSON body expected"))
      case Failure(e) =>
        Logger.error(s"quoteSearch: json = ${request.body.asJson.orNull}", e)
        Future.successful(InternalServerError(e.getMessage))
    }
  }

}
