package com.shocktrade.javascript

import com.github.ldaniels528.scalascript.extensions.{Route, RouteProvider}
import com.github.ldaniels528.scalascript.{Module, Scope, angular}
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
 * ShockTrade.js Application Main
 * @author lawrence.daniels@gmail.com
 */
object ShockTradeJsMain extends js.JSApp {

  def main() {
    // create the application
    val module = angular.createModule("shocktrade",
      js.Array("ngAnimate", "ngCookies", "ngRoute", "ngSanitize", "nvd3ChartDirectives", "toaster", "ui.bootstrap"))

    // add the custom directives
    module.directiveOf[AvatarDirective]("avatar")
    module.directiveOf[ChangeArrowDirective]("changearrow")

    // add the custom filters
    module.filter("abs", Filters.abs)
    module.filter("bigNumber", Filters.bigNumber)
    module.filter("capitalize", Filters.capitalize)
    module.filter("duration", Filters.duration)
    module.filter("escape", Filters.escape)
    module.filter("newsDuration", Filters.newsDuration)
    module.filter("quoteChange", Filters.quoteChange)
    module.filter("quoteNumber", Filters.quoteNumber)
    module.filter("yesno", Filters.yesNo)

    // add the controllers and services
    configureServices(module)
    configureControllers(module)

    // define the routes
    module.config({ ($routeProvider: RouteProvider) =>
      $routeProvider
        .when("/about/investors", Route(templateUrl = "/assets/views/about/investors.htm"))
        .when("/about/me", Route(templateUrl = "/assets/views/about/me.htm"))
        .when("/about/us", Route(templateUrl = "/assets/views/about/us.htm"))
        .when("/dashboard", Route(templateUrl = "/assets/views/dashboard/dashboard.htm", controller = "DashboardController"))
        .when("/dashboard/:contestId", Route(templateUrl = "/assets/views/dashboard/dashboard.htm", controller = "DashboardController"))
        .when("/discover", Route(templateUrl = "/assets/views/discover/discover.htm", controller = "DiscoverController"))
        .when("/discover/:symbol", Route(templateUrl = "/assets/views/discover/discover.htm", controller = "DiscoverController"))
        .when("/explore", Route(templateUrl = "/assets/views/explore/drill_down.htm", controller = "DrillDownController"))
        .when("/home", Route(templateUrl = "/assets/views/profile/home.htm", controller = "HomeController"))
        .when("/inspect/:contestId", Route(templateUrl = "/assets/views/admin/inspect.htm", controller = "InspectController"))
        .when("/news", Route(templateUrl = "/assets/views/news/news_center.htm", controller = "NewsController"))
        .when("/research", Route(templateUrl = "/assets/views/research/research.htm", controller = "ResearchController"))
        .when("/search", Route(templateUrl = "/assets/views/play/search.htm", controller = "GameSearchController"))
        .otherwise(Route(redirectTo = "/about/us"))
      ()
    })

    // initialize the application
    module.run({ ($rootScope: Scope, WebSocketService: WebSocketService) =>
      // inject Facebook's JavaScript SDK
      FacebookInjector.init()

      // initialize the web socket service
      WebSocketService.init()
    })
  }

  private def configureServices(module: Module) {
    module.serviceOf[ConnectService]("ConnectService")
    module.serviceOf[ContestService]("ContestService")
    module.serviceOf[Facebook]("Facebook")
    module.serviceOf[MarketStatusService]("MarketStatus")
    module.serviceOf[MySession]("MySession")
    module.serviceOf[NewsService]("NewsService")
    module.serviceOf[ProfileService]("ProfileService")
    module.serviceOf[QuoteService]("QuoteService")
    module.serviceOf[ResearchService]("ResearchService")
    module.serviceOf[WebSocketService]("WebSocketService")

    module.serviceOf[ComposeMessageDialog]("ComposeMessageDialog")
    module.serviceOf[InvitePlayerDialog]("InvitePlayerDialog")
    module.serviceOf[NewGameDialog]("NewGameDialogService")
    module.serviceOf[NewOrderDialog]("NewOrderDialog")
    module.serviceOf[NewsQuoteDialog]("NewsQuoteDialog")
    module.serviceOf[PerksDialog]("PerksDialog")
    module.serviceOf[SignUpDialog]("SignUpDialog")
    module.serviceOf[TransferFundsDialog]("TransferFundsDialog")
  }

  private def configureControllers(module: Module) {
    module.controllerOf[AwardsController]("AwardsController")
    module.controllerOf[CashAccountController]("CashAccountController")
    module.controllerOf[ChatController]("ChatController")
    module.controllerOf[ConnectController]("ConnectController")
    module.controllerOf[DashboardController]("DashboardController")
    module.controllerOf[DiscoverController]("DiscoverController")
    module.controllerOf[DrillDownController]("DrillDownController")
    module.controllerOf[ExposureController]("ExposureController")
    module.controllerOf[GameSearchController]("GameSearchController")
    module.controllerOf[HomeController]("HomeController")
    module.controllerOf[InspectController]("InspectController")
    module.controllerOf[MainController]("MainController")
    module.controllerOf[MarginAccountController]("MarginAccountController")
    module.controllerOf[MyGamesController]("MyGamesController")
    module.controllerOf[MyQuotesController]("MyQuotesController")
    module.controllerOf[NavigationController]("NavigationController")
    module.controllerOf[NewsController]("NewsController")
    module.controllerOf[PortfolioController]("PortfolioController")
    module.controllerOf[ResearchController]("ResearchController")
    module.controllerOf[TradingHistoryController]("TradingHistoryController")

    module.controllerOf[ComposeMessageDialogController]("ComposeMessageDialogController")
    module.controllerOf[InvitePlayerDialogController]("InvitePlayerDialogController")
    module.controllerOf[NewGameDialogController]("NewGameDialogController")
    module.controllerOf[NewOrderDialogController]("NewOrderDialogController")
    module.controllerOf[NewsQuoteDialogController]("NewsQuoteDialogController")
    module.controllerOf[PerksDialogController]("PerksDialogController")
    module.controllerOf[SignUpDialogController]("SignUpController")
    module.controllerOf[TransferFundsDialogController]("TransferFundsDialogController")
  }

}
