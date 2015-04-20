package com.shocktrade.controllers

import java.util.Date
import com.shocktrade.controllers.BSONObjectHandling
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.GetLastError

import scala.concurrent.Future

/**
 * Blog Resources
 * @author lawrence.daniels@gmail.com
 */
object BlogResources extends Controller with MongoController with MongoExtras with BSONObjectHandling {
  lazy val mc = db.collection[JSONCollection]("Blog")

  def createBlogPost() = Action.async { request =>
    request.body.asJson match {
      case Some(js: JsObject) =>
        val post = JS("creationTime" -> new Date()) ++ js
        for {
          result <- mc.insert(post)
        } yield Ok(JS("result" -> result.errMsg))

      case _ =>
        Future.successful(BadRequest("JSON expected"))
    }
  }

  def deleteBlogPost(id: String) = Action.async {
    mc.delete(id) map (r => Ok(JS("result" -> r.errMsg)))
  }

  def getBlogPost(id: String) = Action.async { request =>
    mc.findOne(id) map (Ok(_))
  }

  def getBlogPosts(limit: Int) = Action.async { request =>
    mc.find(JS()).sort(JS("creationTime" -> -1)).cursor[JsObject].collect[Seq](limit) map (r => Ok(JsArray(r)))
  }

  def updateBlogPost() = Action.async { request =>
    request.body.asJson match {
      case Some(post: JsObject) =>
        val wc = new GetLastError()
        for {
          result <- mc.update(JS("_id" -> (post \ "_id")), post, wc, upsert = false, multi = false)
        } yield Ok(JS("result" -> result.errMsg))

      case _ =>
        Future.successful(BadRequest("JSON expected"))
    }
  }

}