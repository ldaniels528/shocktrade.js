package com.shocktrade.plugins

import com.shocktrade.server.loaders.DataLoadingProcesses
import play.api.{Application, Logger, Play, Plugin}

/**
 * Data Loading Plugin
 * @author lawrence.daniels@gmail.com
 */
class DataLoadingPlugin(app: Application) extends Plugin {

  /**
   * Called when the application starts.
   */
  override def onStart() {
    Logger.info("Data Loading Plugin starting...")

    // if production, start the Stock Quote Update and Trade Claiming processes
    if (Play.isProd(app)) {
      // start the stock quote update process
      DataLoadingProcesses.start()
    }
  }

  /**
   * Called when the application stops.
   */
  override def onStop() {}

}
