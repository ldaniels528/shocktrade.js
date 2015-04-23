package com.shocktrade.models.contest

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * SearchOptions Search Options
 * @author lawrence.daniels@gmail.com
 */
case class SearchOptions(acquaintances: Boolean = false,
                         activeOnly: Boolean = true,
                         available: Boolean = true,
                         friendsOnly: Boolean = false,
                         levelCap: Option[String] = None,
                         levelCapAllowed: Boolean = true,
                         perksAllowed: Boolean = true,
                         ranked: Option[Boolean] = None)

/**
 * SearchOptions Search Options Singleton
 * @author lawrence.daniels@gmail.com
 */
object SearchOptions {

  implicit val searchOptionReads: Reads[SearchOptions] = (
    (__ \ "acquaintances").read[Boolean] and
      (__ \ "activeOnly").read[Boolean] and
      (__ \ "available").read[Boolean] and
      (__ \ "friendsOnly").read[Boolean] and
      (__ \ "levelCap").readNullable[String] and
      (__ \ "levelCapAllowed").read[Boolean] and
      (__ \ "perksAllowed").read[Boolean] and
      (__ \ "ranked").readNullable[Boolean])(SearchOptions.apply _)

  implicit val searchOptionWrites: Writes[SearchOptions] = (
    (__ \ "acquaintances").write[Boolean] and
      (__ \ "activeOnly").write[Boolean] and
      (__ \ "available").write[Boolean] and
      (__ \ "friendsOnly").write[Boolean] and
      (__ \ "levelCap").writeNullable[String] and
      (__ \ "levelCapAllowed").write[Boolean] and
      (__ \ "perksAllowed").write[Boolean] and
      (__ \ "ranked").writeNullable[Boolean])(unlift(SearchOptions.unapply))

  implicit object SearchOptionsReader extends BSONDocumentReader[SearchOptions] {
    def read(doc: BSONDocument) = SearchOptions(
      doc.getAs[Boolean]("acquaintances").get,
      doc.getAs[Boolean]("activeOnly").get,
      doc.getAs[Boolean]("available").get,
      doc.getAs[Boolean]("friendsOnly").get,
      doc.getAs[String]("levelCap"),
      doc.getAs[Boolean]("levelCapAllowed").get,
      doc.getAs[Boolean]("perksAllowed").get,
      doc.getAs[Boolean]("ranked")
    )
  }

  implicit object SearchOptionsWriter extends BSONDocumentWriter[SearchOptions] {
    def write(contest: SearchOptions) = BSONDocument(
      "acquaintances" -> contest.acquaintances,
      "activeOnly" -> contest.activeOnly,
      "available" -> contest.available,
      "friendsOnly" -> contest.friendsOnly,
      "levelCap" -> contest.levelCap,
      "levelCapAllowed" -> contest.levelCapAllowed,
      "perksAllowed" -> contest.perksAllowed,
      "ranked" -> contest.ranked
    )
  }

}