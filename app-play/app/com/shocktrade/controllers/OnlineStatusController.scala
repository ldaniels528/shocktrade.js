package com.shocktrade.controllers

import javax.inject.Inject

import com.shocktrade.actors.WebSockets
import com.shocktrade.actors.WebSockets.UserStateChanged
import com.shocktrade.dao.OnlineStatusDAO
import com.shocktrade.util.BSONHelper._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.json.Json.{obj => JS}
import play.api.mvc.Action
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.Future

/**
  * Online Status Resources
  * @author lawrence.daniels@gmail.com
  */
class OnlineStatusController @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends MongoController with ReactiveMongoComponents with ErrorHandler {
  private val onlineStatusDAO = OnlineStatusDAO(reactiveMongoApi)

  // This collection requires a TTL index to function properly
  // db.OnlineStatuses.createIndex({updatedTime:1}, {expireAfterSeconds:1800})

  ////////////////////////////////////////////////////////////////////////////
  //      API functions
  ////////////////////////////////////////////////////////////////////////////

  def getGroupStatus = Action.async { request =>
    request.body.asText.map(_.split("[,]").map(_.trim)) match {
      case Some(userIDs) =>
        onlineStatusDAO.findGroupStatus(userIDs) map (results => Ok(Json.toJson(results))) recover {
          case e => Ok(createError(e))
        }
      case None =>
        Future.successful(BadRequest("comma delimited string expected"))
    }
  }

  def getStatus(userID: String) = Action.async {
    onlineStatusDAO.findStatus(userID) map {
      case Some(status) => Ok(Json.toJson(status))
      case None => Ok(JS("_id" -> userID.toBSID, "connected" -> false))
    } recover {
      case e => Ok(createError(e))
    }
  }

  def setIsOnline(userID: String) = Action.async {
    setConnectedStatus(userID, newState = true) map (state => Ok(JS("connected" -> state))) recover {
      case e => Ok(createError(e))
    }
  }

  def setIsOffline(userID: String) = Action.async {
    setConnectedStatus(userID, newState = false) map (state => Ok(JS("connected" -> state))) recover {
      case e => Ok(createError(e))
    }
  }

  private def setConnectedStatus(userID: String, newState: Boolean): Future[Boolean] = {
    onlineStatusDAO.setConnectedStatus(userID, newState) map {
      case Some(oldStatus) =>
        if (!oldStatus.getAs[Boolean]("connected").contains(newState)) {
          Logger.info(s"User $userID is now ${if (newState) "Online" else "Offline"}")
          WebSockets ! UserStateChanged(userID, newState)
        }
        newState
      case None =>
        WebSockets ! UserStateChanged(userID, newState)
        newState
    }
  }

}
