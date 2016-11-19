package com.shocktrade.controlpanel.runtime.functions

import com.shocktrade.controlpanel.runtime.Evaluatable

/**
  * Represents a function
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait Function extends Evaluatable {

  def name: String

  def params: Seq[String]

}