package com.shocktrade.controllers

import com.shocktrade.models.contest.AccountTypes._
import com.shocktrade.models.contest.OrderTerms.OrderTerm
import com.shocktrade.models.contest.OrderTypes.{OrderType, _}
import com.shocktrade.models.contest.PerkTypes.PerkType
import com.shocktrade.models.contest.PlayerRef
import com.shocktrade.models.contest.PriceTypes.{PriceType, _}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, __}

import scala.language.{implicitConversions, postfixOps}

/**
 * Contest Resource Forms
 * @author lawrence.daniels@gmail.com
 */
object ContestResourceForms {

  case class ContestCreateForm(name: String,
                               playerId: String,
                               playerName: String,
                               facebookId: String,
                               startingBalance: BigDecimal,
                               startAutomatically: Option[Boolean],
                               duration: Int,
                               friendsOnly: Option[Boolean],
                               invitationOnly: Option[Boolean],
                               levelCapAllowed: Option[Boolean],
                               levelCap: Option[String],
                               perksAllowed: Option[Boolean],
                               robotsAllowed: Option[Boolean])

  implicit val contestFormReads: Reads[ContestCreateForm] = (
    (__ \ "name").read[String] and
      (__ \ "player" \ "id").read[String] and
      (__ \ "player" \ "name").read[String] and
      (__ \ "player" \ "facebookID").read[String] and
      (__ \ "startingBalance").read[BigDecimal] and
      (__ \ "startAutomatically").readNullable[Boolean] and
      (__ \ "duration" \ "value").read[Int] and
      (__ \ "friendsOnly").readNullable[Boolean] and
      (__ \ "invitationOnly").readNullable[Boolean] and
      (__ \ "levelCapAllowed").readNullable[Boolean] and
      (__ \ "levelCap").readNullable[String] and
      (__ \ "perksAllowed").readNullable[Boolean] and
      (__ \ "robotsAllowed").readNullable[Boolean])(ContestCreateForm.apply _)

  /**
   * {"activeOnly":true,"available":false,"perksAllowed":false,"levelCap":"1","levelCapAllowed":true,"friendsOnly":true,"restrictionUsed":true}
   */
  case class ContestSearchForm(activeOnly: Option[Boolean],
                               available: Option[Boolean],
                               friendsOnly: Option[Boolean],
                               invitationOnly: Option[Boolean],
                               levelCap: Option[String],
                               levelCapAllowed: Option[Boolean],
                               perksAllowed: Option[Boolean],
                               robotsAllowed: Option[Boolean])

  implicit val contestSearchFormReads: Reads[ContestSearchForm] = (
    (__ \ "activeOnly").readNullable[Boolean] and
      (__ \ "available").readNullable[Boolean] and
      (__ \ "friendsOnly").readNullable[Boolean] and
      (__ \ "invitationOnly").readNullable[Boolean] and
      (__ \ "levelCap").readNullable[String] and
      (__ \ "levelCapAllowed").readNullable[Boolean] and
      (__ \ "perksAllowed").readNullable[Boolean] and
      (__ \ "robotsAllowed").readNullable[Boolean])(ContestSearchForm.apply _)

  case class JoinContestForm(playerId: String, playerName: String, facebookId: String)

  implicit val joinContestFormReads: Reads[JoinContestForm] = (
    (__ \ "player" \ "id").read[String] and
      (__ \ "player" \ "name").read[String] and
      (__ \ "player" \ "facebookID").read[String])(JoinContestForm.apply _)

  case class MessageForm(sender: PlayerRef, recipient: Option[PlayerRef], text: String)

  implicit val messageFormReads: Reads[MessageForm] = (
    (__ \ "sender").read[PlayerRef] and
      (__ \ "recipient").readNullable[PlayerRef] and
      (__ \ "text").read[String])(MessageForm.apply _)

  case class MarginFundsForm(action: String, amount: Double)

  implicit val marginFundsFormReads: Reads[MarginFundsForm] = (
    (__ \ "action" \ "action").read[String] and
      (__ \ "amount").read[Double])(MarginFundsForm.apply _)

  /**
   * contestId = 553aa9f15dd0bcf00087f6ea, playerId = 51a308ac50c70a97d375a6b2,
   * form = {"emailNotify":true,"symbol":"AMD","limitPrice":2.3,"exchange":"NasdaqCM","volumeAtOrderTime":15001242,"orderType":"BUY",
   * "priceType":"MARKET","orderTerm":"GOOD_FOR_7_DAYS","quantity":"1000"}
   */
  case class OrderForm(symbol: String,
                       exchange: String,
                       limitPrice: BigDecimal,
                       orderType: OrderType,
                       orderTerm: OrderTerm,
                       priceType: PriceType,
                       perks: Option[Seq[PerkType]],
                       quantity: Int,
                       emailNotify: Option[Boolean],
                       partialFulfillment: Option[Boolean],
                       accountType: AccountType)

  implicit val orderFormReads: Reads[OrderForm] = (
    (__ \ "symbol").read[String] and
      (__ \ "exchange").read[String] and
      (__ \ "limitPrice").read[BigDecimal] and
      (__ \ "orderType").read[OrderType] and
      (__ \ "orderTerm").read[OrderTerm] and
      (__ \ "priceType").read[PriceType] and
      (__ \ "perks").readNullable[Seq[PerkType]] and
      (__ \ "quantity").read[Int] and
      (__ \ "emailNotify").readNullable[Boolean] and
      (__ \ "partialFulfillment").readNullable[Boolean] and
      (__ \ "accountType").read[AccountType])(OrderForm.apply _)

  case class QuoteSnapshot(name: String, symbol: String, lastTrade: Double)

  implicit val quoteSnapshotReads: Reads[QuoteSnapshot] = (
    (__ \ "name").read[String] and
      (__ \ "symbol").read[String] and
      (__ \ "lastTrade").read[Double])(QuoteSnapshot.apply _)

}
