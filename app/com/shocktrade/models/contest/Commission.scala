package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.ContestStatus._
import com.shocktrade.models.contest.OrderType.OrderType
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
 * Represents a trading commission
 * @author lawrence.daniels@gmail.com
 */
case class Commission(paid: BigDecimal, paidDate: Date, orderType: OrderType)

/**
 * Commission Singleton
 * @author lawrence.daniels@gmail.com
 */
object Commission {

  implicit val commissionReads: Reads[Commission] = (
    (JsPath \ "paid").read[BigDecimal] and
      (JsPath \ "paidDate").read[Date] and
      (JsPath \ "orderType").read[OrderType])(Commission.apply _)

  implicit val commissionWrites: Writes[Commission] = (
    (JsPath \ "paid").write[BigDecimal] and
      (JsPath \ "paidDate").write[Date] and
      (JsPath \ "orderType").write[OrderType])(unlift(Commission.unapply))

}
