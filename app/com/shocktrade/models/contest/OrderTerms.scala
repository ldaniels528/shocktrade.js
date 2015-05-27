package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.DateUtil
import org.joda.time.DateTime
import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}
import reactivemongo.bson.{BSONHandler, BSONString}

/**
 * Represents an enumeration of Order Terms
 * @author lawrence.daniels@gmail.com
 */
object OrderTerms extends Enumeration {
  type OrderTerm = Value

  val GOOD_FOR_DAY = Value("GOOD_FOR_DAY")
  val GOOD_FOR_3_DAYS = Value("GOOD_FOR_3_DAYS")
  val GOOD_FOR_7_DAYS = Value("GOOD_FOR_7_DAYS")
  val GOOD_FOR_14_DAYS = Value("GOOD_FOR_14_DAYS")
  val GOOD_FOR_30_DAYS = Value("GOOD_FOR_30_DAYS")
  val GOOD_FOR_60_DAYS = Value("GOOD_FOR_60_DAYS")
  val GOOD_UNTIL_CANCELED = Value("GOOD_UNTIL_CANCELED")

  /**
   * Order Term Extensions
   * @param orderTerm the given [[OrderTerm order term]]
   */
  implicit class OrderTermExtensions(val orderTerm: OrderTerm) extends AnyVal {

    def toDate(fromDate: Date = new Date): Option[Date] = {
      val days_? = orderTerm match {
        case GOOD_FOR_DAY => Some(1)
        case GOOD_FOR_3_DAYS => Some(3)
        case GOOD_FOR_7_DAYS => Some(7)
        case GOOD_FOR_14_DAYS => Some(14)
        case GOOD_FOR_30_DAYS => Some(30)
        case GOOD_FOR_60_DAYS => Some(60)
        case GOOD_UNTIL_CANCELED => None
      }
      days_?.map(days => new DateTime(fromDate).plusDays(days).toDate).map(DateUtil.getMidnightTime)
    }

  }

  /**
   * Order Term Format
   * @author lawrence.daniels@gmail.com
   */
  implicit object OrderTermFormat extends Format[OrderTerm] {

    def reads(json: JsValue) = JsSuccess(OrderTerms.withName(json.as[String]))

    def writes(orderTerm: OrderTerm) = JsString(orderTerm.toString)
  }

  /**
   * Order Term Handler
   * @author lawrence.daniels@gmail.com
   */
  implicit object OrderTermHandler extends BSONHandler[BSONString, OrderTerm] {

    def read(string: BSONString) = OrderTerms.withName(string.value)

    def write(orderTerm: OrderTerm) = BSONString(orderTerm.toString)
  }

}
