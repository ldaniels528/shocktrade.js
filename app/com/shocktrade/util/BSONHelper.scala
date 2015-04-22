package com.shocktrade.util

import play.api.Logger
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.{JsError, JsObject, JsSuccess}
import play.modules.reactivemongo.json.BSONFormats
import reactivemongo.bson.{BSONDocument => BS, BSONObjectID}

/**
 * BSON Helper
 * @author lawrence.daniels@gmail.com
 */
object BSONHelper {

  implicit class JsonBsonFieldExtensions(val fields: Seq[String]) extends AnyVal {

    def bsonFields = fields.foldLeft(BS()) { (obj, field) => obj ++ BS(field -> 1) }

    def jsonFields = fields.foldLeft(JS()) { (obj, field) => obj ++ JS(field -> 1) }

  }

  implicit class BSONObjectIDExtensionsA(val id: String) extends AnyVal {

    def asBSID = BSONObjectID(id)

  }

  implicit class BSONObjectIDExtensionsB(val id: Option[BSONObjectID]) extends AnyVal {

    def toBSID = id.getOrElse(BSONObjectID.generate)

  }

  implicit class Json2BsonExtension(val json: JsObject) extends AnyVal {

    def toBson = {
      Logger.info(s"JS = $json")
      var bson = BS()
      json.fieldSet foreach {
        case (key, value) =>
          BSONFormats.toBSON(value) match {
            case JsSuccess(v, p) => bson = bson ++ BS(key -> v)
            case JsError(errors) =>
              errors foreach {
                case (path, messages) =>
                  messages foreach { err =>
                    Logger.error(s"$path: ${err.message}")
                  }
              }
          }
      }
      bson
    }

  }

}
