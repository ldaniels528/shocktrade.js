package com.shocktrade.controlpanel.runtime.functions.builtin

import com.shocktrade.controlpanel.runtime.Scope

/**
  * Created by ldaniels on 11/19/16.
  */
object BuiltinFunctions {
  private val functions = Seq(
    new DaemonsFx(),
    new ExitFx()
  )
  private val mapping = Map(functions map (fx => (fx.name, fx)): _*)

  def contains(name: String) = mapping.contains(name)

  def enrich(scope: Scope) = scope ++= functions

}
