package com.shocktrade.autonomous

import com.shocktrade.common.models.quote.ResearchQuote

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a compiled op-code
  * @param name the name (description) of the opCode
  * @param filter the given quote-filtering function
  */
@ScalaJSDefined
class OpCode(val name: String, val filter: ResearchQuote => Boolean) extends js.Object