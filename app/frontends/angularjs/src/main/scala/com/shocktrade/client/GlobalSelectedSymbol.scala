package com.shocktrade.client

import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.{Controller, Scope}
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Globally Selected Symbol
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait GlobalSelectedSymbol {
  self: Controller =>

  $scope.selectedSymbol = $cookies.getOrElse("symbol", "AMD")

  def $scope: GlobalSelectedSymbolScope

  def $cookies: Cookies

  /////////////////////////////////////////////////////////////////////
  //          Event Listeners
  /////////////////////////////////////////////////////////////////////

  def onSymbolSelected(newSymbol: String, oldSymbol: Option[String]) {
    // purposefully unimplemented
  }

  $scope.$watch("selectedSymbol", (newValue: js.UndefOr[String], oldValue: js.UndefOr[String]) => {
    newValue foreach { newSymbol =>
      console.log(s"The selected symbol has changed to $newSymbol (from ${oldValue.orNull})")
      onSymbolSelected(newSymbol, oldValue.flat.toOption)
      $cookies.put("symbol", newSymbol)
    }
  })

}

/**
 * Globally Selected Symbol Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait GlobalSelectedSymbolScope extends Scope {
  var selectedSymbol: js.UndefOr[String] = js.native

}