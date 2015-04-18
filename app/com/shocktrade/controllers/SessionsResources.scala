package com.shocktrade.controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

/**
 * Session Resources
 * @author lawrence.daniels@gmail.com
 */
object SessionsResources extends Controller with MongoController with MongoExtras {
  lazy val mc: JSONCollection = db.collection[JSONCollection]("UserSessions")

  def getSessions = Action.async {
    mc.findAll() map (Ok(_))
  }

  def deleteSession(id: String) = Action.async {
    mc.delete(id) map (o => Ok(o.toString))
  }

}