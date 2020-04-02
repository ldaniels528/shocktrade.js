package com.shocktrade.client

import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Represents the game current
 * @param userID      the given user ID
 * @param portfolioID the given portfolio ID
 */
class GameState(var userID: js.UndefOr[String] = js.undefined,
                var portfolioID: js.UndefOr[String] = js.undefined) extends js.Object

/**
 * Game State Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object GameState {
  private val gameStateCookie = "GameState"

  /**
   * Game State Enriched
   * @param gameState the host [[GameState]]
   */
  final implicit class GameStateEnriched(val gameState: GameState) extends AnyVal {

    def copy(userID: js.UndefOr[String] = js.undefined,
             portfolioID: js.UndefOr[String] = js.undefined): GameState = {
      new GameState(
        userID = userID ?? gameState.userID,
        portfolioID = portfolioID ?? gameState.portfolioID)
    }

    def setPortfolio(portfolioID: js.UndefOr[String])(implicit $cookies: Cookies): GameState = {
      gameState.portfolioID = portfolioID
      $cookies.putGameState(gameState)
      gameState
    }

    def setUser(userID: js.UndefOr[String])(implicit $cookies: Cookies): GameState = {
      gameState.userID = userID
      $cookies.putGameState(gameState)
      gameState
    }
  }

  /**
   * Cookies Enriched
   * @param $cookies the host [[Cookies]]
   */
  final implicit class CookiesEnriched(val $cookies: Cookies) extends AnyVal {

    @inline
    def getGameState: GameState = {
      val gameState = $cookies.getObject[GameState](gameStateCookie).getOrElse(new GameState())
      console.info(s"get::gameState = ${JSON.stringify(gameState)}")
      gameState
    }

    @inline
    def putGameState(gameState: GameState): Unit = {
      $cookies.putObject(gameStateCookie, gameState)
      console.info(s"put::gameState = ${JSON.stringify(gameState)}")
    }

    @inline
    def removeGameState(): Unit = {
      $cookies.remove(gameStateCookie)
      ()
    }

  }

}
