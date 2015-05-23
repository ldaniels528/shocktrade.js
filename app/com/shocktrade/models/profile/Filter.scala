package com.shocktrade.models.profile

import com.shocktrade.models.profile.TimeFrames.TimeFrame
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, _}

import scala.util.{Failure, Success, Try}

/**
 * Represents a filter
 * @author lawrence.daniels@gmail.com
 */
case class Filter(_id: BSONObjectID = BSONObjectID.generate,
                  name: String,
                  dataSource: String,
                  exchanges: List[String] = Nil,
                  sortField: Option[String] = None,
                  ascending: Option[Boolean] = None,
                  maxResults: Option[Int] = None,
                  timeFrame: Option[TimeFrame] = None,
                  conditions: List[Condition] = Nil)

/**
 * Filter Singleton
 * @author lawrence.daniels@gmail.com
 */
object Filter {

  implicit val filterReads: Reads[Filter] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "name").read[String] and
      (__ \ "dataSource").read[String] and
      (__ \ "exchanges").read[List[String]] and
      (__ \ "sortField").readNullable[String] and
      (__ \ "ascending").readNullable[Boolean] and
      (__ \ "maxResults").readNullable[Int] and
      (__ \ "timeFrame").readNullable[TimeFrame] and
      (__ \ "conditions").read[List[Condition]])(Filter.apply _)

  implicit val filterWrites: Writes[Filter] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "name").write[String] and
      (__ \ "dataSource").write[String] and
      (__ \ "exchanges").write[List[String]] and
      (__ \ "sortField").writeNullable[String] and
      (__ \ "ascending").writeNullable[Boolean] and
      (__ \ "maxResults").writeNullable[Int] and
      (__ \ "timeFrame").writeNullable[TimeFrame] and
      (__ \ "conditions").write[List[Condition]])(unlift(Filter.unapply))

  implicit object FilterReader extends BSONDocumentReader[Filter] {
    def read(doc: BSONDocument) = Try(Filter(
      doc.getAs[BSONObjectID]("_id").get,
      doc.getAs[String]("name").get,
      doc.getAs[String]("dataSource").getOrElse("ONLINE"),
      doc.getAs[List[String]]("exchanges").getOrElse(List("AMEX", "NYSE", "NASDAQ")),
      doc.getAs[String]("sortField"),
      doc.getAs[Boolean]("ascending"),
      doc.getAs[Int]("maxResults"),
      doc.getAs[TimeFrame]("timeFrame"),
      doc.getAs[List[Condition]]("conditions").getOrElse(Nil)
    )) match {
      case Success(v) => v
      case Failure(e) =>
        e.printStackTrace()
        throw new IllegalStateException(e)
    }
  }

  implicit object FilterWriter extends BSONDocumentWriter[Filter] {
    def write(filter: Filter) = BSONDocument(
      "_id" -> filter._id,
      "name" -> filter.name,
      "dataSource" -> filter.dataSource,
      "exchanges" -> filter.exchanges,
      "sortField" -> filter.sortField,
      "ascending" -> filter.ascending,
      "maxResults" -> filter.maxResults,
      "timeFrame" -> filter.timeFrame,
      "conditions" -> filter.conditions
    )
  }

}