package com.shocktrade.models.contest

import play.api.libs.json.{JsString, JsSuccess, JsValue, Format}
import reactivemongo.bson.{BSONString, BSONHandler}

/**
 * Represents the enumeration of contest statuses
 * @author lawrence.daniels@gmail.com
 */
object ContestStatuses extends Enumeration {
  type ContestStatus = Value

  val ACTIVE = Value("ACTIVE")
  val CLOSED = Value("CLOSED")

  /**
   * Contest Status Format
   * @author lawrence.daniels@gmail.com
   */
  implicit object ContestStatusFormat extends Format[ContestStatus] {

    def reads(json: JsValue) = JsSuccess(ContestStatuses.withName(json.as[String]))

    def writes(contestStatus: ContestStatus) = JsString(contestStatus.toString)
  }

  /**
   * Contest Status Handler
   * @author lawrence.daniels@gmail.com
   */
  implicit object ContestStatusHandler extends BSONHandler[BSONString, ContestStatus] {

    def read(string: BSONString) = ContestStatuses.withName(string.value)

    def write(contestStatus: ContestStatus) = BSONString(contestStatus.toString)
  }

}
