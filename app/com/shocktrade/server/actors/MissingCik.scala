package com.shocktrade.server.actors

import reactivemongo.bson.{BSONDocument, BSONDocumentReader}

/**
 * Represents a missing CIK information request
 * @author lawrence.daniels@gmail.com
 */
case class MissingCik(symbol: String, name: String)

/**
 * Missing Cik Singleton
 * @author lawrence.daniels@gmail.com
 */
object MissingCik {

  implicit object MissingCikReader extends BSONDocumentReader[MissingCik] {
    def read(doc: BSONDocument) = MissingCik(
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("name").get
    )
  }

}