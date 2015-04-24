package com.shocktrade.controllers

import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

/**
 * Mongo Extras
 * @author lawrence.daniels@gmail.com
 */
trait MongoExtras {

  def die[S](message: String): S = throw new IllegalStateException(message)

  def missing[S](field: String): S = throw new IllegalStateException(s"Required field '$field' is missing")

  implicit def Option2Boolean[T](option: Option[T]): Boolean = option.isDefined

  implicit class MongoFAQ(mc: JSONCollection) {

    def delete(id: String)(implicit ec: ExecutionContext) = {
      mc.remove(JS("_id" -> BSONObjectID(id)))
    }

    def findAll()(implicit ec: ExecutionContext): Future[JsArray] = {
      mc.find(JS()).cursor[JsObject].collect[Seq]() map (JsArray(_))
    }

    def findOne(id: String)(implicit ec: ExecutionContext): Future[JsArray] = {
      mc.find(JS("_id" -> BSONObjectID(id))).cursor[JsObject].collect[Seq](1) map (JsArray(_))
    }

    def findOneOpt(id: String)(implicit ec: ExecutionContext): Future[Option[JsObject]] = {
      mc.find(JS("_id" -> BSONObjectID(id))).cursor[JsObject].headOption
    }

  }

}