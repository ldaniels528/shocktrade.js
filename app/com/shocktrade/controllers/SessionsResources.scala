package com.shocktrade.controllers

import com.shocktrade.util.BSONHelper._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.Reads._
import play.api.libs.json.{JsArray, JsObject}
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection

/**
 * Session Resources
 * @author lawrence.daniels@gmail.com
 */
object SessionsResources extends Controller with MongoController {
  lazy val mc: JSONCollection = db.collection[JSONCollection]("UserSessions")

  def getSessions = Action.async {
    mc.find(JS()).cursor[JsObject].collect[Seq]() map (JsArray(_)) map (Ok(_))
  }

  def deleteSession(id: String) = Action.async {
    mc.remove(JS("_id" -> id.toBSID)) map (o => Ok(o.toString))
  }

}