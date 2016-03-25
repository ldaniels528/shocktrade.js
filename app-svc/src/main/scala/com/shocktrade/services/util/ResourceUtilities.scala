package com.shocktrade.services.util

/**
 * ShockTrade Resource Utilities
 * @author lawrence.daniels@gmail.com
 */
object ResourceUtilities {

  /**
   * Facilitates the automatically closing of any resource that features
   * a <tt>close()<tt> method.
   */
  implicit class AutoClose[T <: { def close(): Unit }](res: T) {

    /**
     * Executes the block and closes the resource
     */
    def use[S](block: (T) => S): S = {
      try { block(res) } finally { res.close() }
    }
  }

  /**
   * Facilitates the automatically closing of any resource that features
   * a <tt>disconnect()<tt> method.
   */
  implicit class AutoDisconnect[T <: { def disconnect(): Unit }](res: T) {

    /**
     * Executes the block and closes the resource
     */
    def use[S](block: (T) => S): S = {
      try { block(res) } finally { res.disconnect() }
    }
  }
  
  /**
   * Facilitates the automatically closing of any resource that features
   * a <tt>disconnect()<tt> method.
   */
  implicit class AutoShutdown[T <: { def shutdown(): Unit }](res: T) {

    /**
     * Executes the block and closes the resource
     */
    def use[S](block: (T) => S): S = {
      try { block(res) } finally { res.shutdown() }
    }
  }

}