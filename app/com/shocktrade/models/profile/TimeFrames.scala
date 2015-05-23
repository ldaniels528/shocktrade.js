package com.shocktrade.models.profile

import play.api.libs.json.Reads._
import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}
import reactivemongo.bson.{BSONHandler, BSONString}

/**
 * Represents an enumeration of time frames
 * @author lawrence.daniels@gmail.com
 */
object TimeFrames extends Enumeration {
  type TimeFrame = Value

  val ONE_WEEK = Value("ONE_WEEK")
  val TWO_WEEKS = Value("TWO_WEEKS")
  val ONE_MONTH = Value("ONE_MONTH")
  val SIXTY_DAYS = Value("SIXTY_DAYS")
  val NINETY_DAYS = Value("NINETY_DAYS")
  val SIX_MONTHS = Value("SIX_MONTHS")

  /**
   * Time Frame Format
   * @author lawrence.daniels@gmail.com
   */
  implicit object TimeFrameFormat extends Format[TimeFrame] {

    def reads(json: JsValue) = JsSuccess(TimeFrames.withName(json.as[String]))

    def writes(timeFrame: TimeFrame) = JsString(timeFrame.toString)
  }

  /**
   * Time Frame Handler
   * @author lawrence.daniels@gmail.com
   */
  implicit object TimeFrameHandler extends BSONHandler[BSONString, TimeFrame] {

    def read(string: BSONString) = TimeFrames.withName(string.value)

    def write(timeFrame: TimeFrame) = BSONString(timeFrame.toString)
  }

}
