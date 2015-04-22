package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.OrderType._
import com.shocktrade.models.contest.PriceType._
import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Reads, Writes}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson._

/**
 * Represents a stock purchase order
 * @author lawrence.daniels@gmail.com
 */
case class Order(symbol: String,
                 exchange: String,
                 creationTime: Date,
                 expirationTime: Option[Date] = None,
                 orderType: OrderType,
                 price: BigDecimal,
                 priceType: PriceType,
                 processedTime: Option[Date] = None,
                 quantity: Int,
                 commission: BigDecimal,
                 emailNotify: Boolean = false,
                 id: BSONObjectID = BSONObjectID.generate)

/**
 * Order Singleton
 * @author lawrence.daniels@gmail.com
 */
object Order {

  implicit val orderReads: Reads[Order] = (
    (JsPath \ "symbol").read[String] and
      (JsPath \ "exchange").read[String] and
      (JsPath \ "creationTime").read[Date] and
      (JsPath \ "expirationTime").read[Option[Date]] and
      (JsPath \ "orderType").read[OrderType] and
      (JsPath \ "price").read[BigDecimal] and
      (JsPath \ "priceType").read[PriceType] and
      (JsPath \ "processedTime").read[Option[Date]] and
      (JsPath \ "quantity").read[Int] and
      (JsPath \ "commission").read[BigDecimal] and
      (JsPath \ "emailNotify").read[Boolean] and
      (JsPath \ "_id").read[BSONObjectID])(Order.apply _)

  implicit val orderWrites: Writes[Order] = (
    (JsPath \ "symbol").write[String] and
      (JsPath \ "exchange").write[String] and
      (JsPath \ "creationTime").write[Date] and
      (JsPath \ "expirationTime").write[Option[Date]] and
      (JsPath \ "orderType").write[OrderType] and
      (JsPath \ "price").write[BigDecimal] and
      (JsPath \ "priceType").write[PriceType] and
      (JsPath \ "processedTime").write[Option[Date]] and
      (JsPath \ "quantity").write[Int] and
      (JsPath \ "commission").write[BigDecimal] and
      (JsPath \ "emailNotify").write[Boolean] and
      (JsPath \ "_id").write[BSONObjectID])(unlift(Order.unapply))

  implicit object OrderReader extends BSONDocumentReader[Order] {
    def read(doc: BSONDocument) = Order(
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("exchange").get,
      doc.getAs[Date]("creationTime").get,
      doc.getAs[Date]("expirationTime"),
      doc.getAs[OrderType]("orderType").get,
      doc.getAs[BigDecimal]("price").get,
      doc.getAs[PriceType]("priceType").get,
      doc.getAs[Date]("processedTime"),
      doc.getAs[Int]("quantity").get,
      doc.getAs[BigDecimal]("commission").get,
      doc.getAs[Boolean]("emailNotify").get,
      doc.getAs[BSONObjectID]("_id").get
    )
  }

  implicit object OrderWriter extends BSONDocumentWriter[Order] {
    def write(order: Order) = BSONDocument(
      "_id" -> order.id,
      "symbol" -> order.symbol,
      "exchange" -> order.exchange,
      "creationTime" -> order.creationTime,
      "expirationTime" -> order.expirationTime,
      "orderType" -> order.orderType,
      "price" -> order.price,
      "priceType" -> order.priceType,
      "processedTime" -> order.processedTime,
      "quantity" -> order.quantity,
      "commission" -> order.commission,
      "emailNotify" -> order.emailNotify
    )
  }

}
