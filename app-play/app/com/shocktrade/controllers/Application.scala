package com.shocktrade.controllers

import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

import com.shocktrade.actors.WebSocketHandler
import com.shocktrade.controllers.Application.initialized
import com.shocktrade.server.trading.DataLoadingProcesses
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

/**
  * Application Controller
  * @author lawrence.daniels@gmail.com
  */
class Application @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends MongoController with ReactiveMongoComponents {

  // one-time initialization
  if (initialized.compareAndSet(false, true)) {
    DataLoadingProcesses(reactiveMongoApi)
  }

  def index = Action(Ok(assets.views.html.index(UUID.randomUUID().toString)))

  def inspectView = Action(Ok(assets.views.html.admin.Inspect()))

  def webSocket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out => WebSocketHandler.props(out) }

  ///////////////////////////////////////////////////////////////////////////////
  //    About Views
  ///////////////////////////////////////////////////////////////////////////////

  def aboutMe = Action(Ok(assets.views.html.about.AboutMe()))

  def aboutUs = Action(Ok(assets.views.html.about.AboutUs()))

  def investors() = Action(Ok(assets.views.html.about.Investors()))

  ///////////////////////////////////////////////////////////////////////////////
  //    Contest Views
  ///////////////////////////////////////////////////////////////////////////////

  def chatView = Action(Ok(assets.views.html.dashboard.Chat()))

  def dashboardView = Action(Ok(assets.views.html.dashboard.Dashboard()))

  def exposureView = Action(Ok(assets.views.html.dashboard.Exposure()))

  def ordersActiveView = Action(Ok(assets.views.html.dashboard.ActiveOrders()))

  def ordersClosedView = Action(Ok(assets.views.html.dashboard.ClosedOrders()))

  def performanceView = Action(Ok(assets.views.html.dashboard.Performance()))

  def positionsView = Action(Ok(assets.views.html.dashboard.Positions()))

  def searchView = Action(Ok(assets.views.html.search.Search()))

  ///////////////////////////////////////////////////////////////////////////////
  //    Discover Views
  ///////////////////////////////////////////////////////////////////////////////

  def discoverView = Action(Ok(assets.views.html.discover.Discover()))

  def balanceSheetView = Action(Ok(assets.views.html.discover.expanders.BalanceSheet()))

  def dividendsSplitsView = Action(Ok(assets.views.html.discover.expanders.DividendsSplits()))

  def incomeStatementView = Action(Ok(assets.views.html.discover.expanders.IncomeStatement()))

  def newsCenterView = Action(Ok(assets.views.html.news.NewsCenter()))

  def pricePerformanceView = Action(Ok(assets.views.html.discover.expanders.PricePerformance()))

  def shareStatisticsView = Action(Ok(assets.views.html.discover.expanders.ShareStatistics()))

  def valuationMeasuresView = Action(Ok(assets.views.html.discover.expanders.ValuationMeasures()))

  ///////////////////////////////////////////////////////////////////////////////
  //    Explore Views
  ///////////////////////////////////////////////////////////////////////////////

  def exploreView = Action(Ok(assets.views.html.explore.Explore()))

  ///////////////////////////////////////////////////////////////////////////////
  //    Research Views
  ///////////////////////////////////////////////////////////////////////////////

  def researchView = Action(Ok(assets.views.html.research.Research()))

  ///////////////////////////////////////////////////////////////////////////////
  //    Home Views
  ///////////////////////////////////////////////////////////////////////////////

  def connectView = Action(Ok(assets.views.html.connect.Connect()))

  def homeView = Action(Ok(assets.views.html.profile.Home()))

}

/**
  * Application Controller Companion Object
  * @author lawrence.daniels@gmail.com
  */
object Application {
  private val initialized = new AtomicBoolean(false)

}