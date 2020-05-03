package com.shocktrade.client

import com.shocktrade.client.GameStateService._
import com.shocktrade.common.models.quote.Ticker
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

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Favorite Symbols
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def getFavoriteSymbols: js.Array[Ticker] = {
    refreshFavoriteSymbols()
    favoriteSymbols.values.toJSArray
  }

  def isFavoriteSymbol(symbol: String): Boolean = {
    refreshFavoriteSymbols()
    favoriteSymbols.contains(symbol)
  }

  def toggleFavoriteSymbol(symbol: String): Unit = {
    refreshFavoriteSymbols()
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

  def getRecentSymbols: js.Array[Ticker] = {
    refreshRecentSymbols()
    recentSymbols.values.toJSArray
  }

  def isRecentSymbol(symbol: String): Boolean = {
    refreshRecentSymbols()
    recentSymbols.contains(symbol)
  }

  def toggleRecentSymbol(symbol: String): Unit = {
    refreshRecentSymbols()
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
  private val recentSymbolsCookie = "recentSymbols"

}