package com.shocktrade.javascript

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate._
import biz.enef.angulate.ext.{Route, RouteProvider}
import com.shocktrade.javascript.admin._
import com.shocktrade.javascript.dashboard._
import com.shocktrade.javascript.dialogs._
import com.shocktrade.javascript.directives.{AvatarDirective, ChangeArrowDirective}
import com.shocktrade.javascript.discover._
import com.shocktrade.javascript.news._
import com.shocktrade.javascript.profile._
import com.shocktrade.javascript.social._

import scala.scalajs.js

/**
 * Scala Js Main
 * @author lawrence.daniels@gmail.com
 */
object ScalaJsMain extends js.JSApp {

  def main() {
    // create the application
    val module = angular.createModule("shocktrade",
      js.Array("ngAnimate", "ngCookies", "ngRoute", "ngSanitize", "nvd3ChartDirectives", "toaster", "ui.bootstrap"))

    // configure and start the application
    configureDirectives(module)
    configureFilters(module)
    configureServices(module)
    configureControllers(module)
    configureDialogs(module)
    configureRoutes(module)
    runApplication(module)
  }

  private def configureDirectives(module: RichModule) {
    AvatarDirective.init()
    //module.directiveOf[AvatarDirective]
    ChangeArrowDirective.init()
    //module.directiveOf[ChangeArrowDirective]
    //module.directiveOf[EscapeDirective]
  }

  private def configureFilters(module: RichModule) {
    module.filter("abs", Filters.abs)
    module.filter("bigNumber", Filters.bigNumber)
    module.filter("capitalize", Filters.capitalize)
    module.filter("duration", Filters.duration)
    module.filter("escape", Filters.escape)
    module.filter("newsDuration", Filters.newsDuration)
    module.filter("quoteChange", Filters.quoteChange)
    module.filter("quoteNumber", Filters.quoteNumber)
    module.filter("yesno", Filters.yesNo)
  }

  private def configureServices(module: RichModule) {
    module.serviceOf[ConnectService]("ConnectService")
    module.serviceOf[ContestService]("ContestService")
    module.serviceOf[FacebookService]("Facebook")
    module.serviceOf[MarketStatusService]("MarketStatus")
    module.serviceOf[MySession]("MySession")
    module.serviceOf[NewsService]("NewsService")
    module.serviceOf[ProfileService]("ProfileService")
    module.serviceOf[QuoteService]("QuoteService")
    module.serviceOf[WebSocketService]("WebSocketService")
  }

  private def configureControllers(module: RichModule) {
    module.controllerOf[AwardsController]("AwardsController")
    module.controllerOf[CashAccountController]("CashAccountController")
    module.controllerOf[ChatController]("ChatController")
    module.controllerOf[ConnectController]("ConnectController")
    module.controllerOf[DashboardController]("DashboardController")
    module.controllerOf[DiscoverController]("DiscoverController")
    module.controllerOf[DrillDownController]("DrillDownController")
    module.controllerOf[ExposureController]("ExposureController")
    module.controllerOf[FavoritesController]("FavoritesController")
    module.controllerOf[GameSearchController]("GameSearchController")
    module.controllerOf[InspectController]("InspectController")
    module.controllerOf[MainController]("MainController")
    module.controllerOf[MarginAccountController]("MarginAccountController")
    module.controllerOf[MyGamesController]("MyGamesController")
    module.controllerOf[NavigationController]("NavigationController")
    module.controllerOf[NewsController]("NewsController")
    module.controllerOf[PortfolioController]("PortfolioController")
    module.controllerOf[ResearchController]("ResearchController")
    module.controllerOf[StatisticsController]("StatisticsController")
    module.controllerOf[TradingHistoryController]("TradingHistoryController")
  }

  private def configureDialogs(module: RichModule) {
    // ShockTrade dialogs
    module.serviceOf[ComposeMessageDialogService]("ComposeMessageDialog")
    module.controllerOf[ComposeMessageDialogController]("ComposeMessageDialogController")
    module.serviceOf[InvitePlayerDialogService]("InvitePlayerDialog")
    module.controllerOf[InvitePlayerDialogController]("InvitePlayerDialogController")
    module.serviceOf[NewGameDialogService]("NewGameDialogService")
    module.controllerOf[NewGameDialogController]("NewGameDialogController")
    module.serviceOf[NewOrderDialogService]("NewOrderDialog")
    module.controllerOf[NewOrderDialogController]("NewOrderDialogController")
    module.serviceOf[NewsQuoteDialogService]("NewsQuoteDialog")
    module.controllerOf[NewsQuoteDialogController]("NewsQuoteDialogController")
    module.serviceOf[PerksDialogService]("PerksDialog")
    module.controllerOf[PerksDialogController]("PerksDialogController")
    module.serviceOf[SignUpDialogService]("SignUpDialog")
    module.controllerOf[SignUpDialogController]("SignUpController")
    module.serviceOf[TransferFundsDialogService]("TransferFundsDialog")
    module.controllerOf[TransferFundsDialogController]("TransferFundsDialogController")
  }

  private def configureRoutes(module: RichModule) {
    module.config({ ($routeProvider: RouteProvider) =>
      $routeProvider
        .when("/connect", Route(templateUrl = "/assets/views/connect/connect.htm", controller = "ConnectController"))
        .when("/dashboard", Route(templateUrl = "/assets/views/dashboard/dashboard.htm", controller = "DashboardController"))
        .when("/dashboard/:contestId", Route(templateUrl = "/assets/views/dashboard/dashboard.htm", controller = "DashboardController"))
        .when("/discover", Route(templateUrl = "/assets/views/discover/discover.htm", controller = "DiscoverController"))
        .when("/discover/:symbol", Route(templateUrl = "/assets/views/discover/discover.htm", controller = "DiscoverController"))
        .when("/explore", Route(templateUrl = "/assets/views/explore/drill_down.htm", controller = "DrillDownController"))
        .when("/inspect/:contestId", Route(templateUrl = "/assets/views/admin/inspect.htm", controller = "InspectController"))
        .when("/news", Route(templateUrl = "/assets/views/news/news_center.htm", controller = "NewsController"))
        .when("/research", Route(templateUrl = "/assets/views/research/research.htm", controller = "ResearchController"))
        .when("/search", Route(templateUrl = "/assets/views/play/search.htm", controller = "GameSearchController"))
        .when("/symbols/favorites?:symbol", Route(templateUrl = "/assets/views/discover/favorites.htm", /*reloadOnSearch = false,*/ controller = "FavoritesController"))
        .when("/symbols", Route(redirectTo = "/symbols/favorites"))
        .when("/profile/awards", Route(templateUrl = "/assets/views/profile/awards.htm", controller = "AwardsController"))
        .when("/profile/statistics", Route(templateUrl = "/assets/views/profile/statistics.htm", controller = "StatisticsController"))
        .when("/profile", Route(redirectTo = "/profile/awards"))
        .otherwise(Route(redirectTo = "/discover"))
    })
  }

  private def runApplication(module: RichModule) {
    module.run({ ($rootScope: js.Dynamic, MySession: MySession, WebSocketService: WebSocketService) =>
      // capture the session and websocket instances
      $rootScope.MySession = MySession.asInstanceOf[js.Dynamic]

      // inject Facebook's JavaScript SDK
      FacebookInjector.init()

      // initialize the web socket service
      WebSocketService.init()
    })
  }

}
