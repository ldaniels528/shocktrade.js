package com.shocktrade.models.contest

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, _}

/**
 * Represents a message addressee; either a sender or recipient of a message
 * @param id the given player ID
 * @param name the name of the addressee
 * @param facebookId the FaceBook ID of the addressee
 * @author lawrence.daniels@gmail.com
 */
case class Player(id: BSONObjectID, name: String, facebookId: String)

/**
 * Addressee Singleton
 * @author lawrence.daniels@gmail.com
 */
object Player {

  implicit val playerReads: Reads[Player] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "name").read[String] and
      (__ \ "facebookID").read[String])(Player.apply _)

  implicit val playerWrites: Writes[Player] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "name").write[String] and
      (__ \ "facebookID").write[String])(unlift(Player.unapply))

  implicit object PlayerReader extends BSONDocumentReader[Player] {
    def read(doc: BSONDocument) = Player(
      doc.getAs[BSONObjectID]("_id").get,
      doc.getAs[String]("name").get,
      doc.getAs[String]("facebookID").get
    )
  }

  implicit object PlayerWriter extends BSONDocumentWriter[Player] {
    def write(player: Player) = BSONDocument(
      "_id" -> player.id,
      "name" -> player.name,
      "facebookID" -> player.facebookId
    )
  }

}
