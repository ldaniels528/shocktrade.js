package com.shocktrade.models.contest

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
                         friendsOnly: Option[Boolean] = None,
                         levelCap: Option[String] = None,
                         perksAllowed: Option[Boolean] = None,
                         robotsAllowed: Option[Boolean] = None)

/**
 * SearchOptions Search Options Singleton
 * @author lawrence.daniels@gmail.com
 */
object SearchOptions {

  implicit val searchOptionReads: Reads[SearchOptions] = (
    (__ \ "activeOnly").readNullable[Boolean] and
      (__ \ "available").readNullable[Boolean] and
      (__ \ "friendsOnly").readNullable[Boolean] and
      (__ \ "levelCap").readNullable[String] and
      (__ \ "perksAllowed").readNullable[Boolean] and
      (__ \ "robotsAllowed").readNullable[Boolean])(SearchOptions.apply _)

  implicit val searchOptionWrites: Writes[SearchOptions] = (
    (__ \ "activeOnly").writeNullable[Boolean] and
      (__ \ "available").writeNullable[Boolean] and
      (__ \ "friendsOnly").writeNullable[Boolean] and
      (__ \ "levelCap").writeNullable[String] and
      (__ \ "perksAllowed").writeNullable[Boolean] and
      (__ \ "robotsAllowed").writeNullable[Boolean])(unlift(SearchOptions.unapply))

  implicit object SearchOptionsReader extends BSONDocumentReader[SearchOptions] {
    def read(doc: BSONDocument) = SearchOptions(
      doc.getAs[Boolean]("activeOnly"),
      doc.getAs[Boolean]("available"),
      doc.getAs[Boolean]("friendsOnly"),
      doc.getAs[String]("levelCap"),
      doc.getAs[Boolean]("perksAllowed"),
      doc.getAs[Boolean]("robotsAllowed")
    )
  }

  implicit object SearchOptionsWriter extends BSONDocumentWriter[SearchOptions] {
    def write(contest: SearchOptions) = BSONDocument(
      "activeOnly" -> contest.activeOnly,
      "available" -> contest.available,
      "friendsOnly" -> contest.friendsOnly,
      "levelCap" -> contest.levelCap,
      "perksAllowed" -> contest.perksAllowed,
      "robotsAllowed" -> contest.robotsAllowed
    )
  }

}