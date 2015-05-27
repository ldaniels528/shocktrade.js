package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.AccountTypes._
import com.shocktrade.models.contest.OrderTerms._
import com.shocktrade.models.contest.OrderTypes._
import com.shocktrade.models.contest.PriceTypes._
import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, _}

/**
 * Represents a closed stock order
 * @author lawrence.daniels@gmail.com
 */
case class ClosedOrder(id: BSONObjectID = BSONObjectID.generate,
                       symbol: String,
                       exchange: String,
                       creationTime: Date,
                       orderTerm: OrderTerm,
                       processedTime: Date,
                       orderType: OrderType,
                       price: BigDecimal,
                       priceType: PriceType,
                       quantity: Long,
                       commission: BigDecimal,
                       message: String,
                       accountType: AccountType) {

  def cost: BigDecimal = price * quantity + commission

  def expirationTime: Option[Date] = orderTerm.toDate(creationTime)

}

/**
 * Closed Order Singleton
 * @author lawrence.daniels@gmail.com
 */
object ClosedOrder {

  implicit val closedOrderReads: Reads[ClosedOrder] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "symbol").read[String] and
      (__ \ "exchange").read[String] and
      (__ \ "creationTime").read[Date] and
      (__ \ "orderTerm").read[OrderTerm] and
      (__ \ "processedTime").read[Date] and
      (__ \ "orderType").read[OrderType] and
      (__ \ "price").read[BigDecimal] and
      (__ \ "priceType").read[PriceType] and
      (__ \ "quantity").read[Long] and
      (__ \ "commission").read[BigDecimal] and
      (__ \ "message").read[String] and
      (__ \ "accountType").read[AccountType])(ClosedOrder.apply _)

  implicit val closedOrderWrites: Writes[ClosedOrder] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "symbol").write[String] and
      (__ \ "exchange").write[String] and
      (__ \ "creationTime").write[Date] and
      (__ \ "orderTerm").write[OrderTerm] and
      (__ \ "processedTime").write[Date] and
      (__ \ "orderType").write[OrderType] and
      (__ \ "price").write[BigDecimal] and
      (__ \ "priceType").write[PriceType] and
      (__ \ "quantity").write[Long] and
      (__ \ "commission").write[BigDecimal] and
      (__ \ "message").write[String] and
      (__ \ "accountType").write[AccountType])(unlift(ClosedOrder.unapply))

  implicit object ClosedOrderReader extends BSONDocumentReader[ClosedOrder] {
    def read(doc: BSONDocument) = ClosedOrder(
      doc.getAs[BSONObjectID]("_id").get,
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("exchange").get,
      doc.getAs[Date]("creationTime").get,
      doc.getAs[OrderTerm]("orderTerm").get,
      doc.getAs[Date]("processedTime").get,
      doc.getAs[OrderType]("orderType").get,
      doc.getAs[BigDecimal]("price").get,
      doc.getAs[PriceType]("priceType").get,
      doc.getAs[Long]("quantity").get,
      doc.getAs[BigDecimal]("commission").get,
      doc.getAs[String]("message").get,
      doc.getAs[AccountType]("accountType").get
    )
  }

  implicit object ClosedOrderWriter extends BSONDocumentWriter[ClosedOrder] {
    def write(order: ClosedOrder) = BSONDocument(
      "_id" -> order.id,
      "symbol" -> order.symbol,
      "exchange" -> order.exchange,
      "creationTime" -> order.creationTime,
      "orderTerm" -> order.orderTerm,
      "processedTime" -> order.processedTime,
      "orderType" -> order.orderType,
      "price" -> order.price,
      "priceType" -> order.priceType,
      "quantity" -> order.quantity,
      "commission" -> order.commission,
      "message" -> order.message,
      "accountType" -> order.accountType
    )
  }

}
