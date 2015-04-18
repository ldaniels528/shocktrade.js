package com.shocktrade.controllers

import akka.actor.{Actor, ActorRef, Props}
import play.api.Play.current
import play.api.mvc._

import scala.concurrent.Future

/**
 * Application Resources
 * @author lawrence.daniels@gmail.com
 */
object Application extends Controller {

  /**
   * Renders the index page
   */
  def index = Action {
    Ok(views.html.index())
  }

  /**
   * Returns a JavaScript file containing the appropriate Facebook App ID
   * .com '364507947024983'
   * .net '616569495084446'
   * .biz '616941558381179'
   * DEV '522523074535098'
   */
  def facebook() = Action { request =>
    // determine the appropriate Facebook ID
    val appId = request.host.toLowerCase match {
      case s if s.contains("localhost") => "522523074535098" // local dev
      case s if s.contains("shocktrade.biz") => "616941558381179" // shocktrade.biz
      case s if s.contains("shocktrade.com") => "364507947024983" // shocktrade.com
      case s if s.contains("shocktrade.net") => "616569495084446" // shocktrade.net
      case _ => "522523074535098" // unknown, so local dev
    }
    Ok(views.js.facebook(appId))
  }

  /**
   * Handles the chat web socket
   */
  def connect(userName: String) = WebSocket.tryAcceptWithActor[String, String] { request =>
    Future.successful(request.session.get("user") match {
      case None => Left(Forbidden)
      case Some(_) => Right(MyWebSocketActor.props)
    })
  }

  object MyWebSocketActor {
    def props(out: ActorRef) = Props(new MyWebSocketActor(out))
  }

  class MyWebSocketActor(out: ActorRef) extends Actor {
    def receive = {
      case msg: String =>
        out ! ("I received your message: " + msg)
    }
  }

}