package com.shocktrade.javascript

import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

import scala.scalajs.js

/**
 * Represents a Main Tab
 */
@js.native
trait MainTab extends js.Object {
  var name: String
  var icon_class: String
  var tool_tip: String
  var url: String
  var contestRequired: Boolean
  var authenticationRequired: Boolean
}

/**
 * Main Tab Singleton
 */
object MainTab {

  val Tabs = js.Array(
    MainTab(name = "About", icon_class = "fa-info-circle", tool_tip = "About ShockTrade", url = "/about/us"),
    MainTab(name = "Home", icon_class = "fa-home", tool_tip = "My Home page", url = "/home", authenticationRequired = true),
    MainTab(name = "Search", icon_class = "fa-search", tool_tip = "Search for games", url = "/search"),
    MainTab(name = "Dashboard", icon_class = "fa-gamepad", tool_tip = "Main game dashboard", url = "/dashboard", contestRequired = true),
    MainTab(name = "Discover", icon_class = "fa-newspaper-o", tool_tip = "Stock News and Quotes", url = "/discover"),
    MainTab(name = "Explore", icon_class = "fa-trello", tool_tip = "Explore Sectors and Industries", url = "/explore"),
    MainTab(name = "Research", icon_class = "fa-database", tool_tip = "Stock Research", url = "/research"))

  def apply(name: String,
            icon_class: String,
            tool_tip: String,
            url: String,
            contestRequired: Boolean = false,
            authenticationRequired: Boolean = false) = {
    val tab = makeNew[MainTab]
    tab.name = name
    tab.icon_class = icon_class
    tab.tool_tip = tool_tip
    tab.url = url
    tab.contestRequired = contestRequired
    tab.authenticationRequired = authenticationRequired
    tab
  }

}
