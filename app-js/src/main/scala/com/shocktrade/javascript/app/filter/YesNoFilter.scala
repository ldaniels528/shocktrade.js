package com.shocktrade.javascript.app.filter

import com.greencatsoft.angularjs.{injectable, Filter}

/**
 * Yes/No Filter
 * @author lawrence.daniels@gmail.com
 */
@injectable("yesno")
object YesNoFilter extends Filter[String] {
  override def filter(item: String): String = item match {
    case "true" => "Yes"
    case _ => "No"
  }
}
