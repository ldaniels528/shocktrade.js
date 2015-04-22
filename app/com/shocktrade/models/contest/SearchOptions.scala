package com.shocktrade.models.contest

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Reads, Writes}

/**
 * Contest Search Options
 * @author lawrence.daniels@gmail.com
 */
case class SearchOptions(activeOnly: Boolean,
                         available: Boolean,
                         perksAllowed: Boolean,
                         friendsOnly: Boolean,
                         acquaintances: Boolean,
                         levelCap: String,
                         levelCapAllowed: Boolean)

/**
 * Contest Search Options Singleton
 * @author lawrence.daniels@gmail.com
 */
object SearchOptions {

  implicit val searchOptionReads: Reads[SearchOptions] = (
    (JsPath \ "activeOnly").read[Boolean] and
      (JsPath \ "available").read[Boolean] and
      (JsPath \ "perksAllowed").read[Boolean] and
      (JsPath \ "friendsOnly").read[Boolean] and
      (JsPath \ "acquaintances").read[Boolean] and
      (JsPath \ "levelCap").read[String] and
      (JsPath \ "levelCapAllowed").read[Boolean])(SearchOptions.apply _)

  implicit val searchOptionWrites: Writes[SearchOptions] = (
    (JsPath \ "activeOnly").write[Boolean] and
      (JsPath \ "available").write[Boolean] and
      (JsPath \ "perksAllowed").write[Boolean] and
      (JsPath \ "friendsOnly").write[Boolean] and
      (JsPath \ "acquaintances").write[Boolean] and
      (JsPath \ "levelCap").write[String] and
      (JsPath \ "levelCapAllowed").write[Boolean])(unlift(SearchOptions.unapply))

}