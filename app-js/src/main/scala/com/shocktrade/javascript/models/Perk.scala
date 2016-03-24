package com.shocktrade.javascript.models

import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Represents a perk
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Perk extends js.Object {
  var name: String = js.native
  var code: String = js.native
  var description: String = js.native
  var cost: Double = js.native
  var owned: Boolean = js.native
  var selected: Boolean = js.native
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
    val perk = makeNew[Perk]
    perk.name = name
    perk.code = code
    perk.description = description
    perk.cost = cost
    perk.owned = owned
    perk.selected = selected
    perk
  }
}
