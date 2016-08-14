package com.shocktrade.javascript.models.contest

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a perk
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Perk(val name: String,
           val code: String,
           val description: String,
           val cost: Double,
           var owned: Boolean = false,
           var selected: Boolean = false) extends js.Object

