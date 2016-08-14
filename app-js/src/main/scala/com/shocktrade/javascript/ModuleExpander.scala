package com.shocktrade.javascript

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Module Expander
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ModuleExpander extends js.Object {
  var title: String
  var url: String
  var icon: String
  var expanded: Boolean
  var visible: js.Function
}

/**
  * Module Expander Singleton
  * @author lawrence.daniels@gmail.com
  */
object ModuleExpander {

  def apply(title: String,
            url: String,
            icon: String,
            expanded: Boolean = false,
            visible: js.UndefOr[js.Function] = js.undefined) = {
    val expander = New[ModuleExpander]
    expander.title = title
    expander.url = url
    expander.icon = icon
    expander.expanded = expanded
    expander.visible = visible.orNull
    expander
  }

}
