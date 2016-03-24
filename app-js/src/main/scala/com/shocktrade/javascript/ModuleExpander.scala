package com.shocktrade.javascript

import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

import scala.scalajs.js

/**
 * Module Expander
 */
trait ModuleExpander extends js.Object {
  var title: String = js.native
  var url: String = js.native
  var icon: String = js.native
  var expanded: Boolean = js.native
  var visible: js.Function = js.native
}

/**
 * Module Expander Singleton
 */
object ModuleExpander {

  def apply(title: String,
            url: String,
            icon: String,
            expanded: Boolean = false,
            visible: js.UndefOr[js.Function] = js.undefined) = {
    val expander = makeNew[ModuleExpander]
    expander.title = title
    expander.url = url
    expander.icon = icon
    expander.expanded = expanded
    expander.visible = visible.orNull
    expander
  }

}
