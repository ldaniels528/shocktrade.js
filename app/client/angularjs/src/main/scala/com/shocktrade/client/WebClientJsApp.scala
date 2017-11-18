package com.shocktrade.client

import com.shocktrade.client.contest._
import com.shocktrade.client.dialogs._
import com.shocktrade.client.directives._
import com.shocktrade.client.discover._
import com.shocktrade.client.news._
import com.shocktrade.client.posts.{PostController, PostService}
import com.shocktrade.client.profile._
import com.shocktrade.client.social._
import com.shocktrade.common.models.FacebookAppInfo
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.facebook.FacebookService
import io.scalajs.npm.angularjs.uirouter.{RouteProvider, RouteTo}
import io.scalajs.npm.angularjs.{Module, Scope, Timeout, angular}
import io.scalajs.social.facebook.{FB, FacebookAppConfig}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * ShockTrade Web Application Client
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object WebClientJsApp {

  @JSExport
  def main(args: Array[String]): Unit = {
    // create the application
    val module = angular.createModule("shocktrade",
      js.Array("ngAnimate", "ngCookies", "ngRoute", "ngSanitize", "nvd3", "angularFileUpload", "toaster", "ui.bootstrap"))

    // add the custom directives
    module.directiveOf[AvatarDirective]("avatar")
    module.directiveOf[ChangeArrowDirective]("changearrow")
    module.directiveOf[CountryDirective]("country")
    module.directiveOf[NewsDirective]("news")
    module.directiveOf[StockQuoteDirective]("stock-quote")

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
    configureFactories(module)
    configureControllers(module)
    configureDialogs(module)

    // define the routes
    module.config({ ($routeProvider: RouteProvider) =>
      // configure the routes
      $routeProvider
        .when("/about/investors", new RouteTo(templateUrl = "/views/about/investors.html"))
        .when("/about/me", new RouteTo(templateUrl = "/views/about/me.html"))
        .when("/about/us", new RouteTo(templateUrl = "/views/about/us.html"))
        .when("/dashboard", new RouteTo(templateUrl = "/views/dashboard/dashboard.html", controller = classOf[DashboardController].getSimpleName))
        .when("/dashboard/:contestId", new RouteTo(templateUrl = "/views/dashboard/dashboard.html", controller = classOf[DashboardController].getSimpleName))
        .when("/discover", new RouteTo(templateUrl = "/views/discover/discover.html", controller = classOf[DiscoverController].getSimpleName))
        .when("/explore", new RouteTo(templateUrl = "/views/explore/drill_down.html", controller = classOf[ExploreController].getSimpleName, reloadOnSearch = false))
        .when("/home", new RouteTo(templateUrl = "/views/profile/home.html", controller = classOf[HomeController].getSimpleName))
        .when("/news", new RouteTo(templateUrl = "/views/news/news_center.html", controller = classOf[NewsController].getSimpleName))
        .when("/posts", new RouteTo(templateUrl = "/views/posts/index.html", controller = classOf[PostController].getSimpleName))
        .when("/research", new RouteTo(templateUrl = "/views/research/research.html", controller = classOf[ResearchController].getSimpleName))
        .when("/search", new RouteTo(templateUrl = "/views/contest/search.html", controller = classOf[GameSearchController].getSimpleName))
        .otherwise(new RouteTo(redirectTo = "/about/us"))
      ()
    })

    // initialize the application
    module.run({ ($rootScope: Scope, $timeout: Timeout,
                  MySessionService: MySessionService, SocialServices: SocialServices, WebSocketService: WebSocketService) =>
      // configure the Social Network callbacks
      configureSocialNetworkCallbacks($timeout, MySessionService, SocialServices)

      // initialize the web socket service
      WebSocketService.init()
    })
  }

  private def configureDialogs(module: Module) {
    module.serviceOf[ComposeMessageDialog]("ComposeMessageDialog")
    module.serviceOf[InvitePlayerDialog]("InvitePlayerDialog")
    module.serviceOf[NewGameDialog]("NewGameDialog")
    module.serviceOf[NewOrderDialog]("NewOrderDialog")
    module.serviceOf[NewsQuoteDialog]("NewsQuoteDialog")
    module.serviceOf[PerksDialog]("PerksDialog")
    module.serviceOf[ReactiveSearchService]("ReactiveSearchService")
    module.serviceOf[SignUpDialog]("SignUpDialog")
    module.serviceOf[TransferFundsDialog]("TransferFundsDialog")

    module.controllerOf[ComposeMessageDialogController]("ComposeMessageDialogController")
    module.controllerOf[InvitePlayerDialogController]("InvitePlayerDialogController")
    module.controllerOf[NewGameDialogController]("NewGameDialogController")
    module.controllerOf[NewOrderDialogController]("NewOrderDialogController")
    module.controllerOf[NewsQuoteDialogController]("NewsQuoteDialogController")
    module.controllerOf[PerksDialogController]("PerksDialogController")
    module.controllerOf[SignUpDialogController]("SignUpController")
    module.controllerOf[TransferFundsDialogController]("TransferFundsDialogController")
  }

  private def configureFactories(module: Module): Unit = {
    module.factoryOf[UserFactory]("UserFactory")
  }

  private def configureServices(module: Module) {
    module.serviceOf[ChatService]("ChatService")
    module.serviceOf[ContestService]("ContestService")
    module.serviceOf[ExploreService]("ExploreService")
    module.serviceOf[FacebookService]("Facebook")
    module.serviceOf[MarketStatusService]("MarketStatus")
    module.serviceOf[MySessionService]("MySessionService")
    module.serviceOf[NewsService]("NewsService")
    module.serviceOf[PortfolioService]("PortfolioService")
    module.serviceOf[UserProfileService]("UserProfileService")
    module.serviceOf[PostService]("PostService")
    module.serviceOf[QuoteCache]("QuoteCache")
    module.serviceOf[QuoteService]("QuoteService")
    module.serviceOf[ResearchService]("ResearchService")
    module.serviceOf[SocialServices]("SocialServices")
    module.serviceOf[UserService]("UserService")
    module.serviceOf[WebSocketService]("WebSocketService")
  }

  private def configureControllers(module: Module) {
    module.controllerOf[AwardsController]("AwardsController")
    module.controllerOf[CashAccountController]("CashAccountController")
    module.controllerOf[ChatController]("ChatController")
    module.controllerOf[DashboardController]("DashboardController")
    module.controllerOf[DiscoverController]("DiscoverController")
    module.controllerOf[ExploreController]("ExploreController")
    module.controllerOf[ExposureController]("ExposureController")
    module.controllerOf[GameSearchController]("GameSearchController")
    module.controllerOf[HomeController]("HomeController")
    module.controllerOf[InformationBarController]("InformationBarController")
    module.controllerOf[MainController]("MainController")
    module.controllerOf[MarginAccountController]("MarginAccountController")
    module.controllerOf[MyGamesController]("MyGamesController")
    module.controllerOf[MyQuotesController]("MyQuotesController")
    module.controllerOf[NavigationController]("NavigationController")
    module.controllerOf[NewsController]("NewsController")
    module.controllerOf[PortfolioController]("PortfolioController")
    module.controllerOf[PostController]("PostController")
    module.controllerOf[ResearchController]("ResearchController")
    module.controllerOf[TradingHistoryController]("TradingHistoryController")
  }

  private def configureSocialNetworkCallbacks($timeout: Timeout, mySession: MySessionService, socialServices: SocialServices): Unit = {
    socialServices.getFacebookAppInfo onComplete {
      case Success(response) => initializeFacebookApp($timeout, mySession, response.data)
      case Failure(e) => console.error("Error initializing Facebook App")
    }
  }

  private def initializeFacebookApp($timeout: Timeout, mySession: MySessionService, appInfo: FacebookAppInfo): Unit = {
    // setup the initialization callback for Facebook
    js.Dynamic.global.fbAsyncInit = () => {
      console.log("fbAsyncInit: Setting up Facebook integration...")
      val config = FacebookAppConfig(appId = appInfo.appId, status = true, xfbml = true)
      FB.init(config)
      console.log(s"Initialized Facebook SDK (App ID # ${config.appId}) and version (${config.version}) on the Angular Facebook service...")

      FB.getLoginStatus { response =>
        console.log(s"initializeFacebookApp: 'getLoginStatus' response = ${angular.toJson(response)}")
      }
    }
  }

}
