package com.shocktrade.controllers

import java.util.UUID

import com.shocktrade.actors.WebSocketHandler
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc._
import play.modules.reactivemongo.MongoController

/**
 * Application Resources
 * @author lawrence.daniels@gmail.com
 */
object Application extends Controller with MongoController {

  /**
   * Renders the index page
   */
  def index = Action {
    Ok(assets.views.html.index(UUID.randomUUID().toString))
  }

  /**
   * Handles web socket connections
   */
  def webSocket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebSocketHandler.props(out)
  }

}