package com.shocktrade.javascript

import com.shocktrade.javascript.admin._
import com.shocktrade.javascript.dashboard._
import com.shocktrade.javascript.dialogs._
import com.shocktrade.javascript.directives.{AvatarDirective, ChangeArrowDirective}
import com.shocktrade.javascript.discover._
import com.shocktrade.javascript.explore.{ExploreController, ExploreService}
import com.shocktrade.javascript.news._
import com.shocktrade.javascript.profile._
import com.shocktrade.javascript.social._
import org.scalajs.angularjs.facebook.FacebookService
import org.scalajs.angularjs.uirouter.{RouteProvider, RouteTo}
import org.scalajs.angularjs.{Module, Scope, angular}
import org.scalajs.dom._
import org.scalajs.jquery._
import org.scalajs.nodejs.social.facebook.{FB, FacebookAppConfig}
import org.scalajs.nodejs.social.linkedin.IN
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

/**
  * ShockTrade.js Application Main
  * @author lawrence.daniels@gmail.com
  */
object ShockTradeJsMain extends js.JSApp {

  override def main() {
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

    // configure the Social Network callbacks
    configureSocialNetworkCallbacks()

    // add the controllers and services
    configureServices(module)
    configureControllers(module)

    // define the routes
    module.config({ ($routeProvider: RouteProvider) =>
      $routeProvider
        .when("/about/investors", RouteTo(templateUrl = "/assets/views/about/investors.htm"))
        .when("/about/me", RouteTo(templateUrl = "/assets/views/about/me.htm"))
        .when("/about/us", RouteTo(templateUrl = "/assets/views/about/us.htm"))
        .when("/dashboard", RouteTo(templateUrl = "/assets/views/dashboard/dashboard.htm", controller = "DashboardController"))
        .when("/dashboard/:contestId", RouteTo(templateUrl = "/assets/views/dashboard/dashboard.htm", controller = "DashboardController"))
        .when("/discover", RouteTo(templateUrl = "/assets/views/discover/discover.htm", controller = "DiscoverController"))
        .when("/discover/:symbol", RouteTo(templateUrl = "/assets/views/discover/discover.htm", controller = "DiscoverController"))
        .when("/explore", RouteTo(templateUrl = "/assets/views/explore/drill_down.htm", controller = "ExploreController"))
        .when("/home", RouteTo(templateUrl = "/assets/views/profile/home.htm", controller = "HomeController"))
        .when("/inspect/:contestId", RouteTo(templateUrl = "/assets/views/admin/inspect.htm", controller = "InspectController"))
        .when("/news", RouteTo(templateUrl = "/assets/views/news/news_center.htm", controller = "NewsController"))
        .when("/research", RouteTo(templateUrl = "/assets/views/research/research.htm", controller = "ResearchController"))
        .when("/search", RouteTo(templateUrl = "/assets/views/play/search.htm", controller = "GameSearchController"))
        .otherwise(RouteTo(redirectTo = "/about/us"))
      ()
    })

    // initialize the application
    module.run({ ($rootScope: Scope, WebSocketService: WebSocketService) =>
      // initialize the web socket service
      WebSocketService.init()
    })
  }

  private def configureServices(module: Module) {
    module.serviceOf[ConnectService]("ConnectService")
    module.serviceOf[ContestService]("ContestService")
    module.serviceOf[ExploreService]("ExploreService")
    module.serviceOf[FacebookService]("Facebook")
    module.serviceOf[MarketStatusService]("MarketStatus")
    module.serviceOf[MySessionService]("MySessionService")
    module.serviceOf[NewsService]("NewsService")
    module.serviceOf[ProfileService]("ProfileService")
    module.serviceOf[QuoteService]("QuoteService")
    module.serviceOf[ResearchService]("ResearchService")
    module.serviceOf[WebSocketService]("WebSocketService")

    module.serviceOf[ComposeMessageDialog]("ComposeMessageDialog")
    module.serviceOf[InvitePlayerDialog]("InvitePlayerDialog")
    module.serviceOf[NewGameDialog]("NewGameDialog")
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
    module.controllerOf[ExploreController]("ExploreController")
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

  private def configureSocialNetworkCallbacks(): Unit = {
    // setup the initialization callback for Facebook
    g.fbAsyncInit = () => {
      console.log("fbAsyncInit: Setting up Facebook integration...")
      val config = FacebookAppConfig(appId = getFacebookAppID(g.location.hostname.asOpt[String].get), status = true, xfbml = true)
      FB.init(config)
      console.log(s"Initialized Facebook SDK (App ID # ${config.appId}) and version (${config.version}) on the Angular Facebook service...")

      // asynchronously initialize Facebook
      onload(
        success = (mySession: MySessionService) => {
          console.info("Initializing Facebook API...")
          mySession.doFacebookLogin()
        },
        failure = () => console.error("Facebook: The MySessionService service could not be retrieved.")
      )
    }

    // setup the initialization callback for LinkedIn
    g.linkedInInit = () => {
      console.log("linkedInInit: Setting up LinkedIn integration...")

      // asynchronously initialize LinkedIn
      onload(
        success = (mySession: MySessionService) => {
          console.info("Initializing LinkedIn API...")
          mySession.initLinkedIn(IN)
        },
        failure = () => console.error("LinkedIn: The MySessionService service could not be retrieved.")
      )
    }
  }

  private def onload(success: js.Function1[MySessionService, Any], failure: js.Function0[Any]) = {
    val mainElem = angular.element(jQuery("#ShockTradeMain"))
    val $scope = mainElem.scope()
    val injector = mainElem.injector()
    injector.get[MySessionService]("MySessionService").toOption match {
      case Some(mySession) =>
        $scope.$apply(() => success(mySession))
      case None =>
        $scope.$apply(() => failure())
    }
  }

  private def getFacebookAppID(hostname: String) = hostname match {
    case "localhost" => "522523074535098" // local dev
    case "shocktrade.southeastasia.cloudapp.azure.com" => "364507947024983"
    case "www.shocktrade.biz" => "616941558381179"
    case "shocktrade.biz" => "616941558381179"
    case "www.shocktrade.com" => "364507947024983"
    case "shocktrade.com" => "364507947024983"
    case "www.shocktrade.net" => "616569495084446"
    case "shocktrade.net" => "616569495084446"
    case _ =>
      console.log(s"Unrecognized hostname '${g.location.hostname}'")
      "522523074535098" // unknown, so local dev
  }

}
