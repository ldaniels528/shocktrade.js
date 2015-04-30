package com.shocktrade.controllers

import com.shocktrade.actors.WebSocketHandler
import com.shocktrade.models.contest.Contest
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
    Ok(assets.views.html.index())
  }

  /**
   * Returns a JavaScript file containing the appropriate Facebook App ID
   * .com '364507947024983'
   * .net '616569495084446'
   * .biz '616941558381179'
   * DEV '522523074535098'
   */
  def facebook = Action { request =>
    // determine the appropriate Facebook ID
    val appId = request.host.toLowerCase match {
      case s if s.contains("localhost") => "522523074535098" // local dev
      case s if s.contains("shocktrade.biz") => "616941558381179" // shocktrade.biz
      case s if s.contains("shocktrade.com") => "364507947024983" // shocktrade.com
      case s if s.contains("shocktrade.net") => "616569495084446" // shocktrade.net
      case _ => "522523074535098" // unknown, so local dev
    }
    Ok(assets.javascripts.js.facebook(appId))
  }

  /**
   * Angular application main
   * @return
   */
  def ngApp = Action {
    Ok(assets.javascripts.js.app(Contest.MaxPlayers))
  }

  /**
   * Handles web socket connections
   */
  def webSocket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebSocketHandler.props(out)
  }

}