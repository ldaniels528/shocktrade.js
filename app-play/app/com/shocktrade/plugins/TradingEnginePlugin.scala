package com.shocktrade.plugins

import com.shocktrade.server.trading.TradingEngine
import com.shocktrade.server.trading.robots.TradingRobots
import play.api.{Application, Logger, Play, Plugin}

/**
 * Trading Engine Plugin
 * @author lawrence.daniels@gmail.com
 */
class TradingEnginePlugin(app: Application) extends Plugin {

  /**
   * Called when the application starts.
   */
  override def onStart() {
    Logger.info("Trading Engine Plugin starting...")

    // if production, start the Stock Quote Update and Trade Claiming processes
    if (Play.isProd(app)) {
      // start the trading engine
      TradingEngine.start()
    }

    // if development, start the Trading Robots
    if (Play.isDev(app)) {
      // start the trading robots
      TradingRobots.start()

      // start the trading engine
      TradingEngine.start()
    }
  }

  /**
   * Called when the application stops.
   */
  override def onStop() {}

}
