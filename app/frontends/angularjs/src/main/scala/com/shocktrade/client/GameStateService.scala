package com.shocktrade.client

import com.shocktrade.client.GameStateService._
import com.shocktrade.common.models.quote.Ticker
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.cookies.Cookies

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Game State Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class GameStateService($cookies: Cookies) extends Service {
  private var favoriteSymbols = js.Dictionary[Ticker]()
  private var recentSymbols = js.Dictionary[Ticker]()
  private var gameState = new GameState()

  // read the cookies
  refreshFavoriteSymbols()
  refreshGameState()
  refreshRecentSymbols()

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Game State
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def getContestID: js.UndefOr[String] = gameState.contestID

  def setContest(contestID: js.UndefOr[String]): GameState = {
    gameState.contestID = contestID
    updateGameState()
  }

  def getPortfolioID: js.UndefOr[String] = gameState.portfolioID

  def setPortfolio(portfolioID: js.UndefOr[String]): GameState = {
    gameState.portfolioID = portfolioID
    updateGameState()
  }
  
  def getSymbol: js.UndefOr[String] = gameState.symbol

  def setSymbol(symbol: js.UndefOr[String]): GameState = {
    gameState.symbol = symbol
    updateGameState()
  }
  
  def getUserID: js.UndefOr[String] = gameState.userID

  def setUser(userID: js.UndefOr[String]): GameState = {
    gameState.userID = userID
    updateGameState()
  }

  def removeGameState(): Unit = {
    $cookies.remove(gameStateCookie)
    ()
  }

  private def refreshGameState(): Unit = $cookies.getObject[GameState](gameStateCookie).foreach(gameState = _)

  private def updateGameState(): GameState = {
    $cookies.putObject(gameStateCookie, gameState)
    console.info(s"updateGameState::gameState = ${JSON.stringify(gameState)}")
    gameState
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Favorite Symbols
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def getFavoriteSymbols: js.Array[Ticker] = favoriteSymbols.values.toJSArray

  def isFavoriteSymbol(symbol: String): Boolean = favoriteSymbols.contains(symbol)

  def toggleFavoriteSymbol(symbol: String): Unit = {
    if (favoriteSymbols.contains(symbol))
      favoriteSymbols.remove(symbol)
    else
      favoriteSymbols(symbol) = new Ticker(symbol = symbol, exchange = js.undefined, lastTrade = js.undefined, tradeDateTime = js.undefined)
    $cookies.putObject(favoriteSymbolsCookie, favoriteSymbols)
  }

  private def refreshFavoriteSymbols(): Unit = $cookies.getObject[js.Dictionary[Ticker]](favoriteSymbolsCookie) foreach (favoriteSymbols = _)

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Recent Symbols
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def getRecentSymbols: js.Array[Ticker] = recentSymbols.values.toJSArray

  def isRecentSymbol(symbol: String): Boolean = recentSymbols.contains(symbol)

  def toggleRecentSymbol(symbol: String): Unit = {
    if (recentSymbols.contains(symbol))
      recentSymbols.remove(symbol)
    else
      recentSymbols(symbol) = new Ticker(symbol = symbol, exchange = js.undefined, lastTrade = js.undefined, tradeDateTime = js.undefined)
    $cookies.putObject(recentSymbolsCookie, recentSymbols)
  }

  private def refreshRecentSymbols(): Unit = $cookies.getObject[js.Dictionary[Ticker]](recentSymbolsCookie) foreach (recentSymbols = _)

}

/**
 * Game State Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object GameStateService {
  private val favoriteSymbolsCookie = "favoriteSymbols"
  private val gameStateCookie = "GameState"
  private val recentSymbolsCookie = "recentSymbols"

  /**
   * Represents the game current
   * @param contestID   the given contest ID
   * @param userID      the given user ID
   * @param portfolioID the given portfolio ID
   */
  class GameState(var contestID: js.UndefOr[String] = js.undefined,
                  var portfolioID: js.UndefOr[String] = js.undefined,
                  var symbol: js.UndefOr[String] = js.undefined,
                  var userID: js.UndefOr[String] = js.undefined) extends js.Object

}