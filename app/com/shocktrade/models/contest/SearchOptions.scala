package com.shocktrade.models.contest

import com.shocktrade.models.contest.AccessRestrictionType._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * SearchOptions Search Options
 * @author lawrence.daniels@gmail.com
 */
case class SearchOptions(activeOnly: Option[Boolean] = None,
                         available: Option[Boolean] = None,
                         levelCap: Option[String] = None,
                         perksAllowed: Option[Boolean] = None,
                         restriction: Option[AccessRestrictionType] = None)

/**
 * SearchOptions Search Options Singleton
 * @author lawrence.daniels@gmail.com
 */
object SearchOptions {

  implicit val searchOptionReads: Reads[SearchOptions] = (
    (__ \ "activeOnly").readNullable[Boolean] and
      (__ \ "available").readNullable[Boolean] and
      (__ \ "levelCap").readNullable[String] and
      (__ \ "perksAllowed").readNullable[Boolean] and
      (__ \ "restriction").readNullable[AccessRestrictionType])(SearchOptions.apply _)

  implicit val searchOptionWrites: Writes[SearchOptions] = (
    (__ \ "activeOnly").writeNullable[Boolean] and
      (__ \ "available").writeNullable[Boolean] and
      (__ \ "levelCap").writeNullable[String] and
      (__ \ "perksAllowed").writeNullable[Boolean] and
      (__ \ "restriction").writeNullable[AccessRestrictionType])(unlift(SearchOptions.unapply))

  implicit object SearchOptionsReader extends BSONDocumentReader[SearchOptions] {
    def read(doc: BSONDocument) = SearchOptions(
      doc.getAs[Boolean]("activeOnly"),
      doc.getAs[Boolean]("available"),
      doc.getAs[String]("levelCap"),
      doc.getAs[Boolean]("perksAllowed"),
      doc.getAs[AccessRestrictionType]("restriction")
    )
  }

  implicit object SearchOptionsWriter extends BSONDocumentWriter[SearchOptions] {
    def write(contest: SearchOptions) = BSONDocument(
      "activeOnly" -> contest.activeOnly,
      "available" -> contest.available,
      "levelCap" -> contest.levelCap,
      "perksAllowed" -> contest.perksAllowed,
      "restriction" -> contest.restriction
    )
  }

}