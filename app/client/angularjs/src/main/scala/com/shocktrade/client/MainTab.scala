package com.shocktrade.client

import scala.scalajs.js

/**
  * Represents a Main Tab
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class MainTab(val name: String,
              val icon_class: String,
              val tool_tip: String,
              val url: String,
              val contestRequired: Boolean = false,
              val authenticationRequired: Boolean = false) extends js.Object

/**
  * Main Tab Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object MainTab {

  val About = 0
  val Discover = 1
  val NewsFeed = 2
  val Home = 3
  val Search = 4
  val Dashboard = 5
  val Research = 6

  val Tabs = js.Array(
    new MainTab(name = "About", icon_class = "fa-info-circle", tool_tip = "About ShockTrade", url = "/about/us"),
    new MainTab(name = "Discover", icon_class = "fa-globe", tool_tip = "Stock News and Quotes", url = "/discover"),
    new MainTab(name = "NewsFeed", icon_class = "fa-newspaper-o", tool_tip = "My Newsfeed", url = "/posts", authenticationRequired = false),
    new MainTab(name = "Home", icon_class = "fa-home", tool_tip = "My Home page", url = "/home", authenticationRequired = true),
    new MainTab(name = "Search", icon_class = "fa-search", tool_tip = "Search for games", url = "/search"),
    new MainTab(name = "Dashboard", icon_class = "fa-gamepad", tool_tip = "Main game dashboard", url = "/dashboard", contestRequired = true),
    new MainTab(name = "Research", icon_class = "fa-database", tool_tip = "Stock Research", url = "/research"))

}
