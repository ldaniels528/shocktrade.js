package com.shocktrade.models.contest

import java.util.Date

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Reads, Writes}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

/**
 * Represents the performance associated with the sell of a position
 * @author lawrence.daniels@gmail.com
 */
case class Performance(symbol: String,
                       exchange: String,
                       pricePaid: BigDecimal,
                       priceSold: BigDecimal,
                       quantity: Int,
                       commissions: List[Commission] = Nil,
                       purchasedDate: Date,
                       soldDate: Date,
                       id: BSONObjectID = BSONObjectID.generate)

/**
 * Performance Singleton
 * @author lawrence.daniels@gmail.com
 */
object Performance {

  implicit val performanceReads: Reads[Performance] = (
    (JsPath \ "symbol").read[String] and
      (JsPath \ "exchange").read[String] and
      (JsPath \ "pricePaid").read[BigDecimal] and
      (JsPath \ "priceSold").read[BigDecimal] and
      (JsPath \ "quantity").read[Int] and
      (JsPath \ "commission").read[List[Commission]] and
      (JsPath \ "purchasedDate").read[Date] and
      (JsPath \ "soldDate").read[Date] and
      (JsPath \ "_id").read[BSONObjectID])(Performance.apply _)

  implicit val performanceWrites: Writes[Performance] = (
    (JsPath \ "symbol").write[String] and
      (JsPath \ "exchange").write[String] and
      (JsPath \ "pricePaid").write[BigDecimal] and
      (JsPath \ "priceSold").write[BigDecimal] and
      (JsPath \ "quantity").write[Int] and
      (JsPath \ "commission").write[List[Commission]] and
      (JsPath \ "purchasedDate").write[Date] and
      (JsPath \ "soldDate").write[Date] and
      (JsPath \ "_id").write[BSONObjectID])(unlift(Performance.unapply))

}

