package com.shocktrade.common.dao.securities

import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a stock quote reference
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class SecuritiesRef(val _id: js.UndefOr[ObjectID], val symbol: String) extends js.Object

/**
  * Securities Reference Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SecuritiesRef {
  val Fields = Seq("symbol")
}