package com.shocktrade.controllers

import java.util.Date

import com.shocktrade.actors.WebSockets
import com.shocktrade.actors.WebSockets.UserStateChanged
import play.api.Logger
import play.api.libs.json.JsArray
import play.api.libs.json.Json.{obj => JS}
import play.api.mvc.{Action, Controller}

import scala.collection.concurrent.TrieMap

/**
 * Online Status Resources
 * @author lawrence.daniels@gmail.com
 */
object OnlineStatusController extends Controller with ErrorHandler {
  private val statuses = TrieMap[String, OnlineStatus]()

  ////////////////////////////////////////////////////////////////////////////
  //      API functions
  ////////////////////////////////////////////////////////////////////////////

  def getGroupStatus = Action { request =>
    request.body.asText.map(_.split("[,]")) match {
      case Some(userIDs) =>
        val results = userIDs map (userID => JS("userID" -> userID, "connected" -> statuses.get(userID).exists(_.connected)))
        Ok(JsArray(results))
      case None =>
        BadRequest("comma delimited string expected")
    }
  }

  def getStatus(userID: String) = Action { request =>
    Ok(JS("userID" -> userID, "connected" -> statuses.get(userID).exists(_.connected)))
  }

  def setIsOnline(userID: String) = Action { request =>
    Ok(JS("connected" -> setConnectedStatus(userID, newState = true)))
  }

  def setIsOffline(userID: String) = Action { request =>
    Ok(JS("connected" -> setConnectedStatus(userID, newState = false)))
  }

  private def setConnectedStatus(userID: String, newState: Boolean) = {
    val prevState = statuses.get(userID).map(_.connected)
    statuses.put(userID, OnlineStatus(connected = newState))

    if (!prevState.contains(newState)) {
      WebSockets ! UserStateChanged(userID, newState)
    }
    Logger.info(s"User $userID is ${if (newState) "Online" else "Offline"}")
    newState
  }

  case class OnlineStatus(connected: Boolean, eventTime: Date = new Date())

}
