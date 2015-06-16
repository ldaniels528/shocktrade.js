package com.shocktrade.javascript

import biz.enef.angulate._
import com.shocktrade.javascript.admin.InspectController
import com.shocktrade.javascript.dashboard._
import com.shocktrade.javascript.dialogs.PerksDialog.PerksDialogController
import com.shocktrade.javascript.dialogs.TransferFundsDialog.TransferFundsDialogController
import com.shocktrade.javascript.dialogs.{PerksDialog, TransferFundsDialog}
import com.shocktrade.javascript.directives.EscapeDirective
import com.shocktrade.javascript.discover._
import com.shocktrade.javascript.news.{NewsController, NewsService}
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

    // define the global function for retrieving the App ID
    g.getShockTradeAppID = { () => getAppId() }: js.Function0[String]

    // get a reference to the application
    val module = angular.module("shocktrade")

    // ShockTrade directives
    //module.directiveOf[AvatarDirective]
    module.directiveOf[EscapeDirective]

    // ShockTrade filters
    module.filter("abs", Filters.abs)
    module.filter("bigNumber", Filters.bigNumber)
    module.filter("capitalize", Filters.capitalize)
    module.filter("duration", Filters.duration)
    module.filter("escape", Filters.escape)
    module.filter("newsDuration", Filters.newsDuration)
    module.filter("quoteChange", Filters.quoteChange)
    module.filter("quoteNumber", Filters.quoteNumber)
    module.filter("yesno", Filters.yesNo)

    // ShockTrade services
    module.serviceOf[ConnectService]("ConnectService")
    module.serviceOf[ContestService]("ContestService")
    module.serviceOf[FacebookService]("Facebook")
    module.serviceOf[MarketStatusService]("MarketStatus")
    module.serviceOf[MySession]("MySession")
    module.serviceOf[NewsService]("NewsService")
    module.serviceOf[ProfileService]("ProfileService")
    module.serviceOf[QuoteService]("QuoteService")
    module.serviceOf[WebSocketService]("WebSockets")

    // ShockTrade controllers
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
    module.controllerOf[NewsController]("NewsController")
    module.controllerOf[PlayerInfoBarController]("PlayerInfoBarController")
    module.controllerOf[PortfolioController]("PortfolioController")
    module.controllerOf[ResearchController]("ResearchController")
    module.controllerOf[StatisticsController]("StatisticsController")
    module.controllerOf[TradingHistoryController]("TradingHistoryController")

    // dialogs
    module.serviceOf[PerksDialog]("PerksDialog")
    module.controllerOf[PerksDialogController]("PerksDialogController")
    module.serviceOf[TransferFundsDialog]("TransferFundsDialog")
    module.controllerOf[TransferFundsDialogController]("TransferFundsDialogController")

  /**
   * Returns the Facebook application ID based on the running host
   * @return {*}
   */
  def getAppId: js.Function0[String] = () => {
    g.console.log(s"Facebook - hostname: ${g.location.hostname}")
    g.location.hostname.as[String] match {
      case "localhost" => "522523074535098" // local dev
      case "www.shocktrade.biz" => "616941558381179"
      case "shocktrade.biz" => "616941558381179"
      case "www.shocktrade.com" => "364507947024983"
      case "shocktrade.com" => "364507947024983"
      case "www.shocktrade.net" => "616569495084446"
      case "shocktrade.net" => "616569495084446"
      case _ =>
        g.console.log(s"Unrecognized hostname '${g.location.hostname}'")
        "522523074535098" // unknown, so local dev
    }
  }

}
