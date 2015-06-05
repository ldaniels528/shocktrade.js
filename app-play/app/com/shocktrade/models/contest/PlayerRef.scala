package com.shocktrade.models.contest

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, _}

/**
 * Represents a player reference
 * @param id the given player ID
 * @param name the name of the addressee
 * @param facebookId the FaceBook ID of the addressee
 * @author lawrence.daniels@gmail.com
 */
case class PlayerRef(id: BSONObjectID, name: String, facebookId: String)

/**
 * Addressee Singleton
 * @author lawrence.daniels@gmail.com
 */
object PlayerRef {

  implicit val playerInfoReads: Reads[PlayerRef] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "name").read[String] and
      (__ \ "facebookID").read[String])(PlayerRef.apply _)

  implicit val playerInfoWrites: Writes[PlayerRef] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "name").write[String] and
      (__ \ "facebookID").write[String])(unlift(PlayerRef.unapply))

  implicit object PlayerInfoReader extends BSONDocumentReader[PlayerRef] {
    def read(doc: BSONDocument) = PlayerRef(
      doc.getAs[BSONObjectID]("_id").get,
      doc.getAs[String]("name").get,
      doc.getAs[String]("facebookID").get
    )
  }

  implicit object PlayerInfoWriter extends BSONDocumentWriter[PlayerRef] {
    def write(player: PlayerRef) = BSONDocument(
      "_id" -> player.id,
      "name" -> player.name,
      "facebookID" -> player.facebookId
    )
  }

}
