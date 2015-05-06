package com.shocktrade.plugins

import com.shocktrade.server.robots.TradingRobots
import play.api.{Application, Plugin}

/**
 * Trading Engine Plugin
 * @author lawrence.daniels@gmail.com
 */
class TradingEnginePlugin(app: Application) extends Plugin {

  /**
   * Called when the application starts.
   */
  override def onStart() = {
    // start the trading engine
    //QualificationEngine.start()

    // start the trading robots
    TradingRobots.start()
  }

  /**
   * Called when the application stops.
   */
  override def onStop() {}

}
