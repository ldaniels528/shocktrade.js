package com.shocktrade.models.profile

import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument => BS, _}

import scala.util.{Failure, Success, Try}

/**
 * Represents a filter condition
 * @author lawrence.daniels@gmail.com
 */
case class Condition(id: BSONObjectID = BSONObjectID.generate,
                     field: String,
                     operator: String,
                     value: WrappedValue) {

  def asExpression: BS = {
    operator match {
      case "=" => BS(field -> value)
      case "!=" => BS(field -> BS("$ne" -> value))
      case "<" => BS(field -> BS("$lt" -> value))
      case ">" => BS(field -> BS("$gt" -> value))
      case "<=" => BS(field -> BS("$lte" -> value))
      case ">=" => BS(field -> BS("$gte" -> value))
      case _ =>
        Logger.warn(s"Unhandled condition: [$field $operator $value]")
        BS()
    }
  }

}

/**
 * Condition Singleton
 * @author lawrence.daniels@gmail.com
 */
object Condition {

  implicit val conditionReads: Reads[Condition] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "field").read[String] and
      (__ \ "operator").read[String] and
      (__ \ "value").read[WrappedValue])(Condition.apply _)

  implicit val conditionWrites: Writes[Condition] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "field").write[String] and
      (__ \ "operator").write[String] and
      (__ \ "value").write[WrappedValue])(unlift(Condition.unapply))

  implicit object ConditionReader extends BSONDocumentReader[Condition] {
    def read(doc: BS) = Try(Condition(
      doc.getAs[BSONObjectID]("_id").getOrElse(BSONObjectID.generate),
      doc.getAs[String]("field").get,
      doc.getAs[String]("operator").get,
      doc.getAs[WrappedValue]("value").get
    )) match {
      case Success(v) => v
      case Failure(e) =>
        e.printStackTrace()
        throw new IllegalStateException(e)
    }
  }

  implicit object ConditionWriter extends BSONDocumentWriter[Condition] {
    def write(condition: Condition) = BS(
      "_id" -> condition.id,
      "field" -> condition.field,
      "operator" -> condition.operator,
      "value" -> condition.value
    )
  }

}