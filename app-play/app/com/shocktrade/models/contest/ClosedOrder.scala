package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.AccountTypes._
import com.shocktrade.models.contest.OrderTerms._
import com.shocktrade.models.contest.OrderTypes._
import com.shocktrade.models.contest.PriceTypes._
import com.shocktrade.util.BSONHelper.{BigDecimalHandler => BDH}
import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats.{BSONObjectIDFormat => BSONIDF}
import reactivemongo.bson.{BSONObjectID, _}

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

  implicit val ClosedOrderFormat = Json.format[ClosedOrder]

  implicit val ClosedOrderHandler = Macros.handler[ClosedOrder]

}
