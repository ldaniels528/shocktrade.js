package com.shocktrade.client

import io.scalajs.npm.angularjs.Scope

import scala.scalajs.js

/**
 * Global Navigation
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait GlobalNavigation extends js.Object {
  self: Scope =>

  var isVisibleTab: js.Function1[js.UndefOr[MainTab], Boolean] = js.native
  var switchToDiscover: js.Function0[Unit] = js.native
  var switchToGameSearch: js.Function0[Unit] = js.native
  var switchToHome: js.Function0[Unit] = js.native
  var switchToTab: js.Function1[js.UndefOr[Int], Unit] = js.native

}
