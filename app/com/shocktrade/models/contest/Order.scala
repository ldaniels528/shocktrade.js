package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.OrderTypes._
import com.shocktrade.models.contest.PriceTypes._
import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson._

/**
 * Represents a stock purchase order
 * @author lawrence.daniels@gmail.com
 */
case class Order(id: BSONObjectID = BSONObjectID.generate,
                 symbol: String,
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
                 volumeAtOrderTime: Long) {

  def cost: BigDecimal = price * quantity + commission

}

/**
 * Order Singleton
 * @author lawrence.daniels@gmail.com
 */
object Order {

  implicit val orderReads: Reads[Order] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "symbol").read[String] and
      (__ \ "exchange").read[String] and
      (__ \ "creationTime").read[Date] and
      (__ \ "expirationTime").readNullable[Date] and
      (__ \ "orderType").read[OrderType] and
      (__ \ "price").read[BigDecimal] and
      (__ \ "priceType").read[PriceType] and
      (__ \ "processedTime").readNullable[Date] and
      (__ \ "quantity").read[Int] and
      (__ \ "commission").read[BigDecimal] and
      (__ \ "emailNotify").read[Boolean] and
      (__ \ "volumeAtOrderTime").read[Long])(Order.apply _)

  implicit val orderWrites: Writes[Order] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "symbol").write[String] and
      (__ \ "exchange").write[String] and
      (__ \ "creationTime").write[Date] and
      (__ \ "expirationTime").writeNullable[Date] and
      (__ \ "orderType").write[OrderType] and
      (__ \ "price").write[BigDecimal] and
      (__ \ "priceType").write[PriceType] and
      (__ \ "processedTime").writeNullable[Date] and
      (__ \ "quantity").write[Int] and
      (__ \ "commission").write[BigDecimal] and
      (__ \ "emailNotify").write[Boolean] and
      (__ \ "volumeAtOrderTime").write[Long])(unlift(Order.unapply))

  implicit object OrderReader extends BSONDocumentReader[Order] {
    def read(doc: BSONDocument) = Order(
      doc.getAs[BSONObjectID]("_id").get,
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
      doc.getAs[Long]("volumeAtOrderTime").get
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
      "emailNotify" -> order.emailNotify,
      "volumeAtOrderTime" -> order.volumeAtOrderTime
    )
  }

}
