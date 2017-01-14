package com.shocktrade.server.dao.securities

import io.scalajs.npm.mongodb.ObjectID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a stock quote reference
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class SecurityRef(val _id: js.UndefOr[ObjectID],
                  val symbol: String,
                  val exchange: js.UndefOr[String]) extends js.Object

/**
  * Securities Reference Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SecurityRef {
  val Fields = Seq("symbol", "exchange")
}