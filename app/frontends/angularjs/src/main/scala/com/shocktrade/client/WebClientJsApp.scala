package com.shocktrade.client

import com.shocktrade.client.contest._
import com.shocktrade.client.dialogs._
import com.shocktrade.client.directives._
import com.shocktrade.client.discover._
import com.shocktrade.client.news._
import com.shocktrade.client.posts._
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
        .otherwise(new RouteTo(redirectTo = "/home"))
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

  private def configureDialogs(module: Module): Unit = {
    // Compose Message Dialog
    module.serviceOf[ComposeMessageDialog]("ComposeMessageDialog")
    module.controllerOf[ComposeMessageDialogController]("ComposeMessageDialogController")

    // Invite Player Dialog
    module.serviceOf[InvitePlayerDialog]("InvitePlayerDialog")
    module.controllerOf[InvitePlayerDialogController]("InvitePlayerDialogController")

    // New Game Dialog
    module.serviceOf[NewGameDialog]("NewGameDialog")
    module.controllerOf[NewGameDialogController]("NewGameDialogController")

    // New Order Dialog
    module.serviceOf[NewOrderDialog]("NewOrderDialog")
    module.controllerOf[NewOrderDialog.NewOrderDialogController]("NewOrderDialogController")

    // News Quote Dialog
    module.serviceOf[NewsQuoteDialog]("NewsQuoteDialog")
    module.controllerOf[NewsQuoteDialogController]("NewsQuoteDialogController")

    // Order Review Dialog
    module.serviceOf[OrderReviewDialog]("OrderReviewDialog")
    module.controllerOf[OrderReviewDialog.OrderReviewDialogController]("OrderReviewDialogController")

    // Perks Dialog
    module.serviceOf[PerksDialog]("PerksDialog")
    module.controllerOf[PerksDialog.PerksDialogController]("PerksDialogController")

    // Player Profile Dialog
    module.serviceOf[PlayerProfileDialog]("PlayerProfileDialog")
    module.controllerOf[PlayerProfileDialogController]("PlayerProfileDialogController")

    // Position Review Dialog
    module.serviceOf[PositionReviewDialog]("PositionReviewDialog")
    module.controllerOf[PositionReviewDialog.PositionReviewDialogController]("PositionReviewDialogController")

    // Sign In Dialog
    module.serviceOf[SignInDialog]("SignInDialog")
    module.controllerOf[SignInDialogController]("SignInDialogController")

    // Sign Up Dialog
    module.serviceOf[SignUpDialog]("SignUpDialog")
    module.controllerOf[SignUpDialogController]("SignUpDialogController")

    // Stock Quote Dialog
    module.serviceOf[StockQuoteDialog]("StockQuoteDialog")
    module.controllerOf[StockQuoteDialog.StockQuoteDialogController]("StockQuoteDialogController")
    ()
  }

  private def configureFactories(module: Module): Unit = {
    module.factoryOf[ContestFactory]("ContestFactory")
    ()
  }

  private def configureServices(module: Module): Unit = {
    module.serviceOf[AwardService]("AwardService")
    module.serviceOf[ContestService]("ContestService")
    module.serviceOf[ExploreService]("ExploreService")
    module.serviceOf[GameStateService]("GameStateService")
    module.serviceOf[MarketStatusService]("MarketStatusService")
    module.serviceOf[OnlineStatusService]("OnlineStatusService")
    module.serviceOf[PortfolioService]("PortfolioService")
    module.serviceOf[PostService]("PostService")
    module.serviceOf[QuoteCache]("QuoteCache")
    module.serviceOf[QuoteService]("QuoteService")
    module.serviceOf[ReactiveSearchService]("ReactiveSearchService")
    module.serviceOf[ResearchService]("ResearchService")
    module.serviceOf[RSSFeedService]("RSSFeedService")
    module.serviceOf[UserService]("UserService")
    module.serviceOf[WebSocketService]("WebSocketService")
    ()
  }

  private def configureControllers(module: Module): Unit = {
    module.controllerOf[DashboardController]("DashboardController")
    module.controllerOf[DiscoverController]("DiscoverController")
    module.controllerOf[ExploreController]("ExploreController")
    module.controllerOf[ExposureController]("ExposureController")
    module.controllerOf[GameSearchController]("GameSearchController")
    module.controllerOf[HomeController]("HomeController")
    module.controllerOf[InformationBarController]("InformationBarController")
    module.controllerOf[MainController]("MainController")
    module.controllerOf[MyQuotesController]("MyQuotesController")
    module.controllerOf[NavigationController]("NavigationController")
    module.controllerOf[NewsController]("NewsController")
    module.controllerOf[PerksController]("PerksController")
    module.controllerOf[PostController]("PostController")
    module.controllerOf[ResearchController]("ResearchController")
    module.controllerOf[TradingHistoryController]("TradingHistoryController")
    ()
  }

}
