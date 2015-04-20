package com.shocktrade.controllers

import java.util.Date
import com.shocktrade.controllers.BSONObjectHandling
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.{obj => JS}
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future

/**
 * Bug Report Resources
 * @author lawrence.daniels@gmail.com
 */
object BugReportResources extends Controller with MongoController with MongoExtras with BSONObjectHandling {
  lazy val mc = db.collection[JSONCollection]("BugReports")

  /**
   * Returns a trading clock state object
   */
  def createBugReport = Action.async { implicit request =>
    // retrieve the JSON document
    val json = for {
      report <- request.body.asJson
      creationTime <- (report \ "creationTime").asOpt[Date]
      title <- (report \ "title").asOpt[String]
      submittedBy <- (report \ "submittedBy").asOpt[String]
      description <- (report \ "description").asOpt[String]
      priority <- (report \ "priority").asOpt[String]
      resolvedTime <- (report \ "resolvedTime").asOpt[Date]
      resolved <- (report \ "resolved").asOpt[Date]
    } yield JS(
        "creationTime" -> creationTime,
        "title" -> title,
        "submittedBy" -> submittedBy,
        "description" -> description,
        "priority" -> priority,
        "resolvedTime" -> resolvedTime,
        "resolved" -> resolved)

    // write the document to the store
    json match {
      case Some(doc) =>
        mc.save(doc) map (o => Ok(o.toString))
      case None =>
        Future.successful(BadRequest(s"Invalid JSON document: ${request.body.asJson}"))
    }
  }

  def deleteBugReport(id: String) = Action.async {
    mc.delete(id) map (o => Ok(o.toString))
  }

  def getBugReport(id: String) = Action.async {
    mc.findOne(id) map (Ok(_))
  }

  def getBugReports = Action.async {
    mc.findAll() map (Ok(_))
  }

}