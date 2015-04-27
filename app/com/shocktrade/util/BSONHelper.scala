package com.shocktrade.util

import java.util.Date

import org.joda.time.DateTime
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.{JsObject, JsValue}
import reactivemongo.bson.{BSONDateTime, BSONDocument => BS, BSONDouble, BSONHandler, BSONObjectID}

/**
 * BSON Helper
 * @author lawrence.daniels@gmail.com
 */
object BSONHelper {

  implicit class JsonExtensions(val json: JsValue) extends AnyVal {

    def flatId: JsValue = {
      json match {
        case js: JsObject =>
          val id = (js \ "_id" \ "$oid").as[String]
          js ++ JS("_id" -> id)
        case js => js
      }
    }
  }

  /**
   * Big Decimal to BSON Double Handler
   * @author lawrence.daniels@gmail.com
   */
  implicit object BigDecimalHandler extends BSONHandler[BSONDouble, BigDecimal] {

    def read(double: BSONDouble) = BigDecimal(double.value)

    def write(bd: BigDecimal) = BSONDouble(bd.toDouble)
  }

  /**
   * Date to BSON Date Time Handler
   * @author lawrence.daniels@gmail.com
   */
  implicit object DateHandler extends BSONHandler[BSONDateTime, Date] {

    def read(date: BSONDateTime) = new Date(date.value)

    def write(date: Date) = BSONDateTime(date.getTime)
  }

  /**
   * Joda DateTime to BSON Date Time Handler
   * @author lawrence.daniels@gmail.com
   */
  implicit object JodaDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {

    def read(date: BSONDateTime) = new DateTime(date.value)

    def write(date: DateTime) = BSONDateTime(date.toDate.getTime)
  }

  /**
   * JSON/BSON Extension for generating field lists
   * @param fields the given field names
   */
  implicit class JsonBsonFieldExtensions(val fields: Seq[String]) extends AnyVal {

    // TODO revisit the reduced field logic

    def toBsonFields = BS() //fields.foldLeft(BS()) { (obj, field) => obj ++ BS(field -> 1) }

    def toJsonFields = JS() // fields.foldLeft(JS()) { (obj, field) => obj ++ JS(field -> 1) }

  }

  /**
   * BSON Extension for converting a String-based identifier to a BSON Object ID
   * @param id the string representation of the BSON Object ID
   */
  implicit class BSONObjectIDExtensions(val id: String) extends AnyVal {

    def toBSID = BSONObjectID(id)

  }

}
