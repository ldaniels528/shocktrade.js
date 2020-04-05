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
  val Home = 0
  //val Dashboard = 1
  val Search = 1
  val Research = 2
  val Discover = 3

  val Tabs: js.Array[MainTab] = js.Array(
    new MainTab(name = "Home", icon_class = "fa-home", tool_tip = "Home screen", url = "/home"),
    //new MainTab(name = "Play", icon_class = "fa-gamepad", tool_tip = "Play the game", url = "/dashboard/", contestRequired = true),
    new MainTab(name = "Search", icon_class = "fa-search", tool_tip = "Search for games", url = "/search"),
    new MainTab(name = "Research", icon_class = "fa-table", tool_tip = "Stock Research", url = "/research"),
    new MainTab(name = "Discover", icon_class = "fa-bar-chart", tool_tip = "Stock News and Quotes", url = "/discover")
  )

}
