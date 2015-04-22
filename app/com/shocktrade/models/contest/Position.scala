package com.shocktrade.models.contest

import java.util.Date

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Reads, Writes}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

/**
 * Represents a financial position
 * @author lawrence.daniels@gmail.com
 */
case class Position(symbol: String,
                    exchange: String,
                    pricePaid: BigDecimal,
                    quantity: Int,
                    commission: Commission,
                    purchasedDate: Date,
                    id: Option[BSONObjectID] = None)

/**
 * Position Singleton
 * @author lawrence.daniels@gmail.com
 */
object Position {

  implicit val positionReads: Reads[Position] = (
    (JsPath \ "symbol").read[String] and
      (JsPath \ "exchange").read[String] and
      (JsPath \ "pricePaid").read[BigDecimal] and
      (JsPath \ "quantity").read[Int] and
      (JsPath \ "commission").read[Commission] and
      (JsPath \ "purchasedDate").read[Date] and
      (JsPath \ "_id").read[Option[BSONObjectID]])(Position.apply _)

  implicit val positionWrites: Writes[Position] = (
    (JsPath \ "symbol").write[String] and
      (JsPath \ "exchange").write[String] and
      (JsPath \ "pricePaid").write[BigDecimal] and
      (JsPath \ "quantity").write[Int] and
      (JsPath \ "commission").write[Commission] and
      (JsPath \ "purchasedDate").write[Date] and
      (JsPath \ "_id").write[Option[BSONObjectID]])(unlift(Position.unapply))

}
