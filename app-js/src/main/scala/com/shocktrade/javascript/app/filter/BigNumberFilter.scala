package com.shocktrade.javascript.app.filter

import com.greencatsoft.angularjs.{Filter, injectable}

/**
 * Big Number Filter
 * @author lawrence.daniels@gmail.com
 */
@injectable("bigNumber")
object BigNumberFilter extends Filter[String] {
  override def filter(value: String): String = {
    value match {
      //case Some(number) =>
      case _ => ""
    }
  }
}
