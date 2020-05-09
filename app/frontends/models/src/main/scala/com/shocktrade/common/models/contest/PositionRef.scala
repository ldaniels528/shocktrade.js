package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Position Reference
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PositionRef(val positionID: js.UndefOr[String]) extends js.Object

/**
 * Position Reference Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PositionRef {

  def apply(positionID: js.UndefOr[String]): PositionRef = new PositionRef(positionID)

  def unapply(ref: PositionRef): Option[String] = ref.positionID.toOption

}