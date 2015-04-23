package com.shocktrade.models.contest

import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * Represents a message addressee; either a sender or recipient of a message
 * @param name the name of the addressee
 * @param facebookId the FaceBook ID of the addressee
 * @author lawrence.daniels@gmail.com
 */
case class Addressee(name: String, facebookId: String)

/**
 * Addressee Singleton
 * @author lawrence.daniels@gmail.com
 */
object Addressee {

  implicit val senderReads: Reads[Addressee] = (
    (__ \ "name").read[String] and
      (__ \ "facebookID").read[String])(Addressee.apply _)

  implicit val senderWrites: Writes[Addressee] = (
    (__ \ "name").write[String] and
      (__ \ "facebookID").write[String])(unlift(Addressee.unapply))

  implicit object AddresseeReader extends BSONDocumentReader[Addressee] {
    def read(doc: BSONDocument) = Addressee(
      doc.getAs[String]("name").get,
      doc.getAs[String]("facebookID").get
    )
  }

  implicit object AddresseeWriter extends BSONDocumentWriter[Addressee] {
    def write(addressee: Addressee) = BSONDocument(
      "name" -> addressee.name,
      "facebookID" -> addressee.facebookId
    )
  }

}
