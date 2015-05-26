package com.shocktrade.models.contest

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

/**
 * Represents a contest player ranking
 * @author lawrence.daniels@gmail.com
 */
case class Ranking(id: BSONObjectID,
                   name: String,
                   facebookID: String,
                   totalEquity: BigDecimal,
                   gainLoss_% : BigDecimal)

/**
 * Ranking Singleton
 * @author lawrence.daniels@gmail.com
 */
object Ranking {

  implicit val rankingReads: Reads[Ranking] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "name").read[String] and
      (__ \ "facebookID").read[String] and
      (__ \ "totalEquity").read[BigDecimal] and
      (__ \ "gainLoss").read[BigDecimal])(Ranking.apply _)

  implicit val rankingWrites: Writes[Ranking] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "name").write[String] and
      (__ \ "facebookID").write[String] and
      (__ \ "totalEquity").write[BigDecimal] and
      (__ \ "gainLoss").write[BigDecimal])(unlift(Ranking.unapply))

}
