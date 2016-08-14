package com.shocktrade.javascript.models

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Represents a perk
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Perk extends js.Object {
  var name: String
  var code: String
  var description: String
  var cost: Double
  var owned: Boolean
  var selected: Boolean
}

/**
  * Perk Singleton
  */
object Perk {

  def apply(name: String,
            code: String,
            description: String,
            cost: Double,
            owned: Boolean = false,
            selected: Boolean = false) = {
    val perk = New[Perk]
    perk.name = name
    perk.code = code
    perk.description = description
    perk.cost = cost
    perk.owned = owned
    perk.selected = selected
    perk
  }
}
