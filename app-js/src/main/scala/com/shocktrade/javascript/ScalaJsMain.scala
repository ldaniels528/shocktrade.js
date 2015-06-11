package com.shocktrade.javascript

import biz.enef.angulate._
import com.shocktrade.javascript.dashboard.{DashboardController, ContestService, GameSearchController, MyGamesController}
import com.shocktrade.javascript.discover.{MarketStatusService, QuoteService}
import com.shocktrade.javascript.news.{ChatController, NewsController}
import com.shocktrade.javascript.profile._

import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.JSApp

/**
 * Scala Js Main
 * @author lawrence.daniels@gmail.com
 */
object ScalaJsMain extends JSApp {

  def main() {
    g.console.log("ScalaJS Main method is executing...")
    val module = angular.module("shocktrade")

    // ShockTrade directives
    //module.directiveOf[Avatar]

    // ShockTrade filters
    module.filter("bigNumber", Filters.bigNumber)
    module.filter("capitalize", Filters.capitalize)
    module.filter("duration", Filters.duration)
    module.filter("escape", Filters.escape)
    module.filter("quoteChange", Filters.quoteChange)
    module.filter("quoteNumber", Filters.quoteNumber)
    module.filter("yesno", Filters.yesNo)

    // ShockTrade services
    module.serviceOf[ConnectService]("ConnectService")
    module.serviceOf[ContestService]("ContestService")
    module.serviceOf[FacebookService]("Facebook")
    module.serviceOf[MarketStatusService]("MarketStatus")
    module.serviceOf[MySession]("MySession")
    module.serviceOf[ProfileService]("ProfileService")
    module.serviceOf[QuoteService]("QuoteService")
    module.serviceOf[WebSocketService]("WebSockets")

    // ShockTrade controllers
    module.controllerOf[AwardsController]("AwardsController")
    module.controllerOf[ChatController]("ChatController")
    module.controllerOf[ConnectController]("ConnectController")
    module.controllerOf[DashboardController]("DashboardController")
    module.controllerOf[GameSearchController]("GameSearchController")
    module.controllerOf[MyGamesController]("MyGamesController")
    module.controllerOf[NewsController]("NewsController")
    module.controllerOf[StatisticsController]("StatisticsController")
  }

}
