package com.shocktrade.client.models.contest

import scala.scalajs.js

/**
  * Represents a Perk
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class Perk(val name: String,
           val code: String,
           val description: String,
           val cost: Double,
           var owned: Boolean = false,
           var selected: Boolean = false) extends js.Object

