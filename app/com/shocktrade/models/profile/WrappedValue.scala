package com.shocktrade.models.profile

import play.api.libs.json._
import reactivemongo.bson._

/**
 * Represents a wrapped value (text or numeric)
 * @author lawrence.daniels@gmail.com
 */
case class WrappedValue(value: Either[String, BigDecimal])

/**
 * Wrapped Value Singleton
 * @author lawrence.daniels@gmail.com
 */
object WrappedValue {

  def apply(value: String): WrappedValue = WrappedValue(Left(value))

  def apply(value: BigDecimal): WrappedValue = WrappedValue(Right(value))

  /**
   * Wrapped Value Format
   * @author lawrence.daniels@gmail.com
   */
  implicit object WrappedValueFormat extends Format[WrappedValue] {

    def reads(json: JsValue) = json match {
      case js: JsString => JsSuccess(WrappedValue(Left(js.value)))
      case js: JsNumber => JsSuccess(WrappedValue(Right(js.value)))
      case js => JsError(s"Invalid type $js")
    }

    def writes(wrappedValue: WrappedValue) = wrappedValue.value match {
      case Left(s) => JsString(s)
      case Right(n) => JsNumber(n)
    }
  }

  /**
   * Wrapped Value Reader
   * @author lawrence.daniels@gmail.com
   */
  implicit object WrappedValueReader extends BSONDocumentReader[WrappedValue] {
    def read(doc: BSONDocument) =
      doc.getAs[BSONValue]("value").get match {
        case BSONString(s) => WrappedValue(s)
        case BSONDouble(n) => WrappedValue(n)
      }
  }

  /**
   * Wrapped Value Writer
   * @author lawrence.daniels@gmail.com
   */
  implicit object WrappedValueWriter extends BSONDocumentWriter[WrappedValue] {
    def write(value: WrappedValue) = value match {
      case WrappedValue(Left(s)) => BSONDocument("value" -> BSONString(s))
      case WrappedValue(Right(n)) => BSONDocument("value" -> BSONDouble(n.toDouble))
    }
  }

}