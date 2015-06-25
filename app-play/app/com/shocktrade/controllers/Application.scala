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

  def index = Action(Ok(assets.views.html.index(UUID.randomUUID().toString)))

  def aboutUs = Action(Ok(assets.views.html.AboutUs()))

  def chatView() = Action(Ok(assets.views.html.dashboard.Chat()))

  def connectView() = Action(Ok(assets.views.html.connect.Connect()))

  def dashboardView() = Action(Ok(assets.views.html.dashboard.Dashboard()))

  def discoverView() = Action(Ok(assets.views.html.discover.Discover()))

  def exploreView() = Action(Ok(assets.views.html.discover.Explore()))

  def homeView() = Action(Ok(assets.views.html.profile.Home()))

  def exposureView() = Action(Ok(assets.views.html.dashboard.Exposure()))

  def inspectView() = Action(Ok(assets.views.html.admin.Inspect()))

  def newsCenterView = Action(Ok(assets.views.html.news.NewsCenter()))

  def ordersActiveView() = Action(Ok(assets.views.html.dashboard.ActiveOrders()))

  def ordersClosedView() = Action(Ok(assets.views.html.dashboard.ClosedOrders()))

  def performanceView() = Action(Ok(assets.views.html.dashboard.Performance()))

  def positionsView() = Action(Ok(assets.views.html.dashboard.Positions()))

  def researchView() = Action(Ok(assets.views.html.research.Research()))

  def searchView() = Action(Ok(assets.views.html.search.Search()))

  def webSocket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out => WebSocketHandler.props(out) }

}