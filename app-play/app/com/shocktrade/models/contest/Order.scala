package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.AccountTypes._
import com.shocktrade.models.contest.OrderTerms.OrderTerm
import com.shocktrade.models.contest.OrderTypes._
import com.shocktrade.models.contest.PriceTypes._
import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson._

/**
 * Represents a stock order (buy or sell)
 * @author lawrence.daniels@gmail.com
 */
case class Order(id: BSONObjectID = BSONObjectID.generate,
                 symbol: String,
                 exchange: String,
                 creationTime: Date,
                 orderTerm: OrderTerm,
                 orderType: OrderType,
                 price: BigDecimal,
                 priceType: PriceType,
                 quantity: Long,
                 commission: BigDecimal,
                 emailNotify: Boolean = false,
                 partialFulfillment: Boolean = false,
                 accountType: AccountType) {

  def cost: BigDecimal = price * quantity + commission

  def expirationTime: Option[Date] = orderTerm.toDate(creationTime)

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
      (__ \ "orderTerm").read[OrderTerm] and
      (__ \ "orderType").read[OrderType] and
      (__ \ "price").read[BigDecimal] and
      (__ \ "priceType").read[PriceType] and
      (__ \ "quantity").read[Long] and
      (__ \ "commission").read[BigDecimal] and
      (__ \ "emailNotify").read[Boolean] and
      (__ \ "partialFulfillment").read[Boolean] and
      (__ \ "accountType").read[AccountType])(Order.apply _)

  implicit val orderWrites: Writes[Order] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "symbol").write[String] and
      (__ \ "exchange").write[String] and
      (__ \ "creationTime").write[Date] and
      (__ \ "orderTerm").write[OrderTerm] and
      (__ \ "orderType").write[OrderType] and
      (__ \ "price").write[BigDecimal] and
      (__ \ "priceType").write[PriceType] and
      (__ \ "quantity").write[Long] and
      (__ \ "commission").write[BigDecimal] and
      (__ \ "emailNotify").write[Boolean] and
      (__ \ "partialFulfillment").write[Boolean] and
      (__ \ "accountType").write[AccountType])(unlift(Order.unapply))

  implicit object OrderReader extends BSONDocumentReader[Order] {
    def read(doc: BSONDocument) = Order(
      doc.getAs[BSONObjectID]("_id").get,
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("exchange").get,
      doc.getAs[Date]("creationTime").get,
      doc.getAs[OrderTerm]("orderTerm").get,
      doc.getAs[OrderType]("orderType").get,
      doc.getAs[BigDecimal]("price").get,
      doc.getAs[PriceType]("priceType").get,
      doc.getAs[Long]("quantity").get,
      doc.getAs[BigDecimal]("commission").get,
      doc.getAs[Boolean]("emailNotify").get,
      doc.getAs[Boolean]("partialFulfillment").getOrElse(false),
      doc.getAs[AccountType]("accountType").get
    )
  }

  implicit object OrderWriter extends BSONDocumentWriter[Order] {
    def write(order: Order) = BSONDocument(
      "_id" -> order.id,
      "symbol" -> order.symbol,
      "exchange" -> order.exchange,
      "creationTime" -> order.creationTime,
      "orderTerm" -> order.orderTerm,
      "orderType" -> order.orderType,
      "price" -> order.price,
      "priceType" -> order.priceType,
      "quantity" -> order.quantity,
      "commission" -> order.commission,
      "emailNotify" -> order.emailNotify,
      "partialFulfillment" -> order.partialFulfillment,
      "accountType" -> order.accountType
    )
  }

}
