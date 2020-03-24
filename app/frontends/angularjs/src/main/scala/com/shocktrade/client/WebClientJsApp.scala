package com.shocktrade.client

import com.shocktrade.client.contest._
import com.shocktrade.client.dialogs._
import com.shocktrade.client.directives._
import com.shocktrade.client.discover._
import com.shocktrade.client.news._
import com.shocktrade.client.posts._
import com.shocktrade.client.social._
import com.shocktrade.client.users._
import io.scalajs.npm.angularjs.uirouter.{RouteProvider, RouteTo}
import io.scalajs.npm.angularjs.{Module, QProvider, angular}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
 * ShockTrade Web Application Client
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object WebClientJsApp {

  @JSExport
  def main(args: Array[String]): Unit = {
    // create the application
    val app = angular.createModule("shocktrade",
      js.Array("ngAnimate", "ngCookies", "ngRoute", "ngSanitize", "nvd3", "angularFileUpload", "toaster", "ui.bootstrap"))

    // add the custom directives
    app.directiveOf[AvatarDirective]("avatar")
    app.directiveOf[ChangeArrowDirective]("changearrow")
    app.directiveOf[CountryDirective]("country")
    app.directiveOf[NewsDirective]("news")
    app.directiveOf[StockQuoteDirective]("stock-quote")

    // add the custom filters
    app.filter("abs", Filters.abs)
    app.filter("bigNumber", Filters.bigNumber)
    app.filter("capitalize", Filters.capitalize)
    app.filter("duration", Filters.duration)
    app.filter("escape", Filters.escape)
    app.filter("newsDuration", Filters.newsDuration)
    app.filter("quoteChange", Filters.quoteChange)
    app.filter("quoteNumber", Filters.quoteNumber)
    app.filter("yesno", Filters.yesNo)

    // add the controllers and services
    configureServices(app)
    configureFactories(app)
    configureControllers(app)
    configureDialogs(app)

    // define the routes
    app.config { ($routeProvider: RouteProvider, $qProvider: QProvider) =>
      // setup the rejection handler
      $qProvider.errorOnUnhandledRejections(true)

      // configure the routes
      $routeProvider
        .when("/about/investors", new RouteTo(templateUrl = "/views/about/investors.html"))
        .when("/about/me", new RouteTo(templateUrl = "/views/about/me.html"))
        .when("/about/us", new RouteTo(templateUrl = "/views/about/us.html"))
        .when("/dashboard", new RouteTo(templateUrl = "/views/dashboard/dashboard.html", controller = classOf[DashboardController].getSimpleName))
        .when("/dashboard/:contestID", new RouteTo(templateUrl = "/views/dashboard/dashboard.html", controller = classOf[DashboardController].getSimpleName))
        .when("/discover", new RouteTo(templateUrl = "/views/discover/discover.html", controller = classOf[DiscoverController].getSimpleName))
        .when("/discover/:symbol", new RouteTo(templateUrl = "/views/discover/discover.html", controller = classOf[DiscoverController].getSimpleName))
        .when("/explore", new RouteTo(templateUrl = "/views/explore/drill_down.html", controller = classOf[ExploreController].getSimpleName, reloadOnSearch = false))
        .when("/home", new RouteTo(templateUrl = "/views/profile/home.html", controller = classOf[HomeController].getSimpleName))
        .when("/news", new RouteTo(templateUrl = "/views/news/news_center.html", controller = classOf[NewsController].getSimpleName))
        .when("/posts", new RouteTo(templateUrl = "/views/posts/index.html", controller = classOf[PostController].getSimpleName))
        .when("/research", new RouteTo(templateUrl = "/views/research/research.html", controller = classOf[ResearchController].getSimpleName))
        .when("/search", new RouteTo(templateUrl = "/views/contest/search.html", controller = classOf[GameSearchController].getSimpleName))
        .otherwise(new RouteTo(redirectTo = "/discover"))
      ()
    }

    // initialize the application
    app.run({ ($rootScope: RootScope, WebSocketService: WebSocketService) =>
      // initialize the web socket service
      WebSocketService.init()
      ()
    })
    ()
  }

  private def configureDialogs(module: Module) {
    module.serviceOf[ComposeMessageDialog]("ComposeMessageDialog")
    module.serviceOf[InvitePlayerDialog]("InvitePlayerDialog")
    module.serviceOf[NewGameDialog]("NewGameDialog")
    module.serviceOf[NewOrderDialog]("NewOrderDialog")
    module.serviceOf[NewsQuoteDialog]("NewsQuoteDialog")
    module.serviceOf[PerksDialog]("PerksDialog")
    module.serviceOf[ReactiveSearchService]("ReactiveSearchService")
    module.serviceOf[SignInDialog]("SignInDialog")
    module.serviceOf[SignUpDialog]("SignUpDialog")

    module.controllerOf[ComposeMessageDialogController]("ComposeMessageDialogController")
    module.controllerOf[InvitePlayerDialogController]("InvitePlayerDialogController")
    module.controllerOf[NewGameDialogController]("NewGameDialogController")
    module.controllerOf[NewOrderDialogController]("NewOrderDialogController")
    module.controllerOf[NewsQuoteDialogController]("NewsQuoteDialogController")
    module.controllerOf[PerksDialogController]("PerksDialogController")
    module.controllerOf[SignInDialogController]("SignInDialogController")
    module.controllerOf[SignUpDialogController]("SignUpDialogController")
    ()
  }

  private def configureFactories(module: Module): Unit = {
    module.factoryOf[ContestFactory]("ContestFactory")
    module.factoryOf[GameStateFactory]("GameStateFactory")
    ()
  }

  private def configureServices(module: Module) {
    module.serviceOf[AuthenticationService]("AuthenticationService")
    module.serviceOf[AwardService]("AwardService")
    module.serviceOf[ContestService]("ContestService")
    module.serviceOf[ExploreService]("ExploreService")
    module.serviceOf[MarketStatusService]("MarketStatusService")
    module.serviceOf[NewsService]("NewsService")
    module.serviceOf[PortfolioService]("PortfolioService")
    module.serviceOf[UserService]("UserService")
    module.serviceOf[PostService]("PostService")
    module.serviceOf[QuoteCache]("QuoteCache")
    module.serviceOf[QuoteService]("QuoteService")
    module.serviceOf[ResearchService]("ResearchService")
    module.serviceOf[SocialServices]("SocialServices")
    module.serviceOf[UserService]("UserService")
    module.serviceOf[WebSocketService]("WebSocketService")
    ()
  }

  private def configureControllers(module: Module) {
    module.controllerOf[ActiveOrdersController]("ActiveOrdersController")
    module.controllerOf[AwardsController]("AwardsController")
    module.controllerOf[ChatController]("ChatController")
    module.controllerOf[ClosedOrdersController]("ClosedOrdersController")
    module.controllerOf[DashboardController]("DashboardController")
    module.controllerOf[DiscoverController]("DiscoverController")
    module.controllerOf[ExploreController]("ExploreController")
    module.controllerOf[ExposureController]("ExposureController")
    module.controllerOf[GameSearchController]("GameSearchController")
    module.controllerOf[HomeController]("HomeController")
    module.controllerOf[InformationBarController]("InformationBarController")
    module.controllerOf[MainController]("MainController")
    module.controllerOf[MyGamesController]("MyGamesController")
    module.controllerOf[MyQuotesController]("MyQuotesController")
    module.controllerOf[NavigationController]("NavigationController")
    module.controllerOf[NewsController]("NewsController")
    module.controllerOf[PositionsController]("PositionsController")
    module.controllerOf[PostController]("PostController")
    module.controllerOf[ResearchController]("ResearchController")
    module.controllerOf[TradingHistoryController]("TradingHistoryController")
    ()
  }

}
