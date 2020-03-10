package com.shocktrade.cli.runtime.functions

import com.shocktrade.cli.runtime.Evaluatable

/**
 * Represents a function
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait Function extends Evaluatable {

  def name: String

  def params: Seq[String]

}