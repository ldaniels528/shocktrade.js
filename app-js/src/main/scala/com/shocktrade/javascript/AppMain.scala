package com.shocktrade.javascript

import com.greencatsoft.angularjs.core._
import com.greencatsoft.angularjs.{Angular, Config}
import com.shocktrade.javascript.app.filter.YesNoFilter
import com.shocktrade.javascript.dashboard.{ContestServiceFactory, DashboardController, ExplorerServiceFactory}
import com.shocktrade.javascript.discover._
import com.shocktrade.javascript.news.{NewsServiceFactory, NewsSymbolsFactory}

import scala.language.experimental.macros
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

/**
 * ShockTrade Scala.js Application
 * @author lawrence.daniels@gmail.com
 */
@JSExport
object AppMain extends JSApp {

  /**
   * Angular.js application definition
   */
  override def main() {
    val app = Angular.module("shocktrade", Seq("ngAnimate", "ngCookies", "ngRoute", "ngSanitize", "nvd3ChartDirectives", "toaster", "ui.bootstrap"))
    app.config[RoutingConfig]

    // filters
    app.filter(YesNoFilter)

    // controllers
    app.controller[DashboardController]
    app.controller[DiscoverController]

    // services
    app.factory[ContestServiceFactory]
    app.factory[ExplorerServiceFactory]
    app.factory[FavoriteSymbolsFactory]
    app.factory[HeldSecuritiesFactory]
    app.factory[NewsServiceFactory]
    app.factory[NewsSymbolsFactory]
    app.factory[QuoteServiceFactory]
    app.factory[RecentSymbolsFactory]
  }

}

/**
 * Routing Config
 * @param routeProvider the given [[RouteProvider route provider]]
 */
@JSExport
class RoutingConfig(routeProvider: RouteProvider) extends Config {
  routeProvider
    .when("/connect", Route(templateUrl = "/assets/views/connect/connect.htm"))
    .when("/dashboard", Route(templateUrl = "/assets/views/dashboard/dashboard.htm"))
    .when("/dashboard/:contestId", Route(templateUrl = "/assets/views/dashboard/dashboard.htm"))
    .when("/discover", Route(templateUrl = "/assets/views/discover/discover.htm"))
    .when("/discover/:symbol", Route(templateUrl = "/assets/views/discover/discover.htm"))
    .when("/explore", Route(templateUrl = "/assets/views/explore/drill_down.htm"))
    .when("/inspect/:contestId", Route(templateUrl = "/assets/views/admin/inspect.htm"))
    .when("/news", Route(templateUrl = "/assets/views/news/news_center.htm"))
    .when("/research", Route(templateUrl = "/assets/views/research/research.htm"))
    .when("/search", Route(templateUrl = "/assets/views/play/search.htm"))
    .when("/symbols/favorites?:symbol", Route(templateUrl = "/assets/views/discover/favorites.htm" /* reloadOnSearch = false*/))
    .when("/symbols", Route("/symbols/favorites"))
    .when("/profile/awards", Route(templateUrl = "/assets/views/profile/awards.htm"))
    .when("/profile/statistics", Route(templateUrl = "/assets/views/profile/statistics.htm"))
    .when("/profile", Route("/profile/awards"))
    .otherwise(Route("/discover"))
}
