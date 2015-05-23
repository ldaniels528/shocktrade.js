package com.shocktrade.models.contest

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}
import reactivemongo.bson.{BSONHandler, BSONString}

/**
 * An enumeration of perk types
 * @author lawrence.daniels@gmail.com
 */
object PerkTypes extends Enumeration {
  type PerkType = Value

  /**
   * Fee Waiver: Reduces the commissions the player pays for buying or selling securities
   */
  val FEEWAIVR = Value("FEEWAIVR")

  /**
   * Rational People think at the Margin: Gives the player the ability to use margin accounts
   */
  val MARGIN = Value("MARGIN")

  /**
   * Perfect Timing: Gives the player the ability to create BUY orders for more than cash currently available
   */
  val PRFCTIMG = Value("PRFCTIMG")

  /**
   * Purchase Eminent: Gives the player the ability to create SELL orders for securities not yet owned
   */
  val PRCHEMNT = Value("PRCHEMNT")

  /**
   * Perk Type Format
   * @author lawrence.daniels@gmail.com
   */
  implicit object PerkTypeFormat extends Format[PerkType] {

    def reads(json: JsValue) = JsSuccess(PerkTypes.withName(json.as[String]))

    def writes(orderType: PerkType) = JsString(orderType.toString)
  }

  /**
   * Perk Type Handler
   * @author lawrence.daniels@gmail.com
   */
  implicit object PerkTypeHandler extends BSONHandler[BSONString, PerkType] {

    def read(string: BSONString) = PerkTypes.withName(string.value)

    def write(orderType: PerkType) = BSONString(orderType.toString)
  }

}
