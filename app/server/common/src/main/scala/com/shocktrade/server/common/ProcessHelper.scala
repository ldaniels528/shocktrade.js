package com.shocktrade.server.common

import org.scalajs.nodejs.globals.Process
import org.scalajs.sjs.OptionHelper._

/**
  * ShockTrade Process Helper
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ProcessHelper {

  /**
    * Process configuration extensions
    * @param process the given [[Process process]]
    */
  implicit class ProcessConfigExtensions(val process: Process) extends AnyVal {

    /**
      * Attempts to returns the web application listen port
      * @return the option of the web application listen port
      */
    @inline
    def port = process.env.get("port") ?? process.env.get("PORT")

    /**
      * Attempts to returns the database connection URL
      * @return the option of the database connection URL
      */
    @inline
    def dbConnect = process.env.get("db_connection") ?? process.env.get("DB_CONNECTION")

    /**
      * Attempts to returns the Zookeeper connection URL
      * @return the option of the Zookeeper connection URL
      */
    @inline
    def zookeeperConnect = process.env.get("zk_connection") ?? process.env.get("ZK_CONNECTION")

  }

}
