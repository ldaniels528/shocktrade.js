package com.shocktrade.plugins

import com.shocktrade.server.TradingEngine
import play.api.{Application, Plugin}

/**
 * Trading Engine Plugin
 * @author lawrence.daniels@gmail.com
 */
class TradingEnginePlugin(app: Application) extends Plugin {

  /**
   * Called when the application starts.
   */
  override def onStart() = TradingEngine.init()

  /**
   * Called when the application stops.
   */
  override def onStop() {}

}
