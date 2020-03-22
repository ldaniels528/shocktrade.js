package com.shocktrade.common

import scala.scalajs.js

/**
 * Represents a successful update operation
 * @param updateCount the given update count
 */
class Ok(updateCount: js.UndefOr[Int] = js.undefined) extends js.Object

/**
 * Ok Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Ok {

  /**
   * Represents a successful update operation
   * @param updateCount the given update count
   */
  def apply(updateCount: js.UndefOr[Int] = js.undefined): Ok = new Ok(updateCount)

}