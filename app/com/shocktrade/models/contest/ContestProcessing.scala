package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, _}

/**
 * ContestProcessing Processing
 * @author lawrence.daniels@gmail.com
 */
case class ContestProcessing(processedTime: Option[Date] = None,
                             lastMarketClose: Option[Date] = None,
                             host: Option[String] = None)

/**
 * ContestProcessing Processing Singleton
 * @author lawrence.daniels@gmail.com
 */
object ContestProcessing {

  implicit val contestProcessingReads: Reads[ContestProcessing] = (
    (__ \ "processedTime").readNullable[Date] and
      (__ \ "lastMarketClose").readNullable[Date] and
      (__ \ "host").readNullable[String])(ContestProcessing.apply _)

  implicit val contestProcessingtWrites: Writes[ContestProcessing] = (
    (__ \ "processedTime").writeNullable[Date] and
      (__ \ "lastMarketClose").writeNullable[Date] and
      (__ \ "host").writeNullable[String])(unlift(ContestProcessing.unapply))

  implicit object ContestProcessingReader extends BSONDocumentReader[ContestProcessing] {
    def read(doc: BSONDocument) = ContestProcessing(
      doc.getAs[Date]("processedTime"),
      doc.getAs[Date]("lastMarketClose"),
      doc.getAs[String]("host")
    )
  }

  implicit object ContestProcessingWriter extends BSONDocumentWriter[ContestProcessing] {
    def write(contest: ContestProcessing) = BSONDocument(
      "processedTime" -> contest.processedTime,
      "lastMarketClose" -> contest.lastMarketClose,
      "host" -> contest.host
    )
  }

}
