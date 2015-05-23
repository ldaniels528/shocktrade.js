package com.shocktrade.controllers

import com.shocktrade.controllers.QuoteResources._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
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
  lazy val mcQ = db.collection[JSONCollection]("Stocks")
  private val fields = JS(
    "symbol" -> 1, "exchange" -> 1, "lastTrade" -> 1, "open" -> 1, "close" -> 1,
    "changePct" -> 1, "low" -> 1, "high" -> 1, "spread" -> 1, "volume" -> 1
  )

  def quoteSearch = Action.async { implicit request =>
    Try(request.body.asJson.map(_.as[SearchForm])) match {
      case Success(Some(form)) =>
        mcQ.find(form.makeQuery, fields)
          .cursor[JsObject].collect[Seq](250) map (js => Ok(Json.toJson(js)))
      case Success(None) =>
        Logger.info(s"json = ${request.body.asJson.orNull}")
        Future.successful(BadRequest("Proper JSON body expected"))
      case Failure(e) =>
        Logger.error(s"json = ${request.body.asJson.orNull}", e)
        Future.successful(InternalServerError(e.getMessage))
    }
  }

  case class SearchForm(changeMin: Option[Double],
                        changeMax: Option[Double],
                        spreadMin: Option[Double],
                        spreadMax: Option[Double],
                        marketCapMin: Option[Double],
                        marketCapMax: Option[Double],
                        priceMin: Option[Double],
                        priceMax: Option[Double],
                        volumeMin: Option[Long],
                        volumeMax: Option[Long],
                        maxResult: Option[Int]) {

    def makeQuery = {
      var js = JS("active" -> true)
      changeMin.foreach(v => js = js ++ JS("changePct" -> JS("$gte" -> v)))
      changeMax.foreach(v => js = js ++ JS("changePct" -> JS("$lte" -> v)))
      marketCapMin.foreach(v => js = js ++ JS("marketCap" -> JS("$gte" -> v)))
      marketCapMax.foreach(v => js = js ++ JS("marketCap" -> JS("$lte" -> v)))
      priceMin.foreach(v => js = js ++ JS("lastTrade" -> JS("$gte" -> v)))
      priceMax.foreach(v => js = js ++ JS("lastTrade" -> JS("$lte" -> v)))
      spreadMin.foreach(v => js = js ++ JS("spread" -> JS("$gte" -> v)))
      spreadMax.foreach(v => js = js ++ JS("spread" -> JS("$lte" -> v)))
      volumeMin.foreach(v => js = js ++ JS("volume" -> JS("$gte" -> v)))
      volumeMax.foreach(v => js = js ++ JS("volume" -> JS("$lte" -> v)))
      Logger.info(s"query: $js")
      js
    }

  }

  implicit val searchFormReads: Reads[SearchForm] = (
    (__ \ "changeMin").readNullable[Double] and
      (__ \ "changeMax").readNullable[Double] and
      (__ \ "spreadMin").readNullable[Double] and
      (__ \ "spreadMax").readNullable[Double] and
      (__ \ "marketCapMin").readNullable[Double] and
      (__ \ "marketCapMax").readNullable[Double] and
      (__ \ "priceMin").readNullable[Double] and
      (__ \ "priceMax").readNullable[Double] and
      (__ \ "volumeMin").readNullable[Long] and
      (__ \ "volumeMax").readNullable[Long] and
      (__ \ "maxResult").readNullable[Int])(SearchForm.apply _)

}
