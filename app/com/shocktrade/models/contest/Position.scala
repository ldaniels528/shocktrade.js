package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.AccountTypes.AccountType
import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
 * Represents a financial position
 * @author lawrence.daniels@gmail.com
 */
case class Position(id: BSONObjectID = BSONObjectID.generate,
                    symbol: String,
                    exchange: String,
                    pricePaid: BigDecimal,
                    quantity: Long,
                    commission: BigDecimal,
                    processedTime: Date,
                    accountType: AccountType) {

  /**
   * Returns the total cost of the position
   * @return the total cost of the position
   */
  def cost: BigDecimal = pricePaid * quantity + commission

}

/**
 * Position Singleton
 * @author lawrence.daniels@gmail.com
 */
object Position {

  implicit val positionReads: Reads[Position] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "symbol").read[String] and
      (__ \ "exchange").read[String] and
      (__ \ "pricePaid").read[BigDecimal] and
      (__ \ "quantity").read[Long] and
      (__ \ "commission").read[BigDecimal] and
      (__ \ "processedTime").read[Date] and
      (__ \ "accountType").read[AccountType])(Position.apply _)

  implicit val positionWrites: Writes[Position] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "symbol").write[String] and
      (__ \ "exchange").write[String] and
      (__ \ "pricePaid").write[BigDecimal] and
      (__ \ "quantity").write[Long] and
      (__ \ "commission").write[BigDecimal] and
      (__ \ "processedTime").write[Date] and
      (__ \ "accountType").write[AccountType])(unlift(Position.unapply))

  implicit object PositionReader extends BSONDocumentReader[Position] {
    def read(doc: BSONDocument) = Position(
      doc.getAs[BSONObjectID]("_id").get,
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("exchange").get,
      doc.getAs[BigDecimal]("pricePaid").get,
      doc.getAs[Long]("quantity").get,
      doc.getAs[BigDecimal]("commission").get,
      doc.getAs[Date]("processedTime").get,
      doc.getAs[AccountType]("accountType").getOrElse(AccountTypes.CASH)
    )
  }

  implicit object PositionWriter extends BSONDocumentWriter[Position] {
    def write(position: Position) = BSONDocument(
      "_id" -> position.id,
      "symbol" -> position.symbol,
      "exchange" -> position.exchange,
      "pricePaid" -> position.pricePaid,
      "quantity" -> position.quantity,
      "commission" -> position.commission,
      "processedTime" -> position.processedTime,
      "accountType" -> position.accountType
    )
  }

}
