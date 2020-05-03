package com.shocktrade.client.users

import com.shocktrade.client.GameStateService
import io.scalajs.npm.angularjs.{Controller, Scope}

import scala.scalajs.js

/**
 * Personal Symbol Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PersonalSymbolSupport {
  ref: Controller =>

  $scope.heldSymbols = js.Array()

  ///////////////////////////////////////////////////////////////////////////
  //          Injected Variables
  ///////////////////////////////////////////////////////////////////////////

  def $scope: PersonalSymbolSupportScope

  def gameStateService: GameStateService

  ///////////////////////////////////////////////////////////////////////////
  //          Ticker CRUD Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.toggleFavoriteSymbol = (aSymbol: js.UndefOr[String]) => aSymbol.foreach(gameStateService.toggleFavoriteSymbol)

  $scope.isFavoriteSymbol = (aSymbol: js.UndefOr[String]) => aSymbol.exists(gameStateService.isFavoriteSymbol)

  $scope.isHeldSymbol = (aSymbol: js.UndefOr[String]) => aSymbol.exists($scope.heldSymbols.contains)

  $scope.toggleRecentSymbol = (aSymbol: js.UndefOr[String]) => aSymbol.foreach(gameStateService.toggleRecentSymbol)

  $scope.isRecentSymbol = (aSymbol: js.UndefOr[String]) => aSymbol.exists(gameStateService.isRecentSymbol)

}

/**
 * Personal Symbol Support Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait PersonalSymbolSupportScope extends Scope {
  // variables
  var heldSymbols: js.Array[String] = js.native

  // favorite symbols functions
  var toggleFavoriteSymbol: js.Function1[js.UndefOr[String], Unit] = js.native
  var isFavoriteSymbol: js.Function1[js.UndefOr[String], Boolean] = js.native

  // held symbols functions
  var isHeldSymbol: js.Function1[js.UndefOr[String], Boolean] = js.native

  // recently-viewed symbols functions
  var toggleRecentSymbol: js.Function1[js.UndefOr[String], Unit] = js.native
  var isRecentSymbol: js.Function1[js.UndefOr[String], Boolean] = js.native

}