package com.shocktrade.common.forms

import scala.scalajs.js

/**
 * Research Options
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ResearchOptions(var changeMax: js.UndefOr[Double] = js.undefined,
                      var changeMin: js.UndefOr[Double] = js.undefined,
                      var priceMax: js.UndefOr[Double] = js.undefined,
                      var priceMin: js.UndefOr[Double] = js.undefined,
                      var spreadMax: js.UndefOr[Double] = js.undefined,
                      var spreadMin: js.UndefOr[Double] = js.undefined,
                      var volumeMax: js.UndefOr[Double] = js.undefined,
                      var volumeMin: js.UndefOr[Double] = js.undefined,
                      var sortBy: js.UndefOr[String] = js.undefined,
                      var reverse: js.UndefOr[Boolean] = js.undefined,
                      var maxResults: js.UndefOr[Int] = js.undefined) extends js.Object

/**
 * Research Options Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ResearchOptions {

  final implicit class ResearchOptionsEnriched(val options: ResearchOptions) extends AnyVal {

    def toQueryString: String = {
      import options._
      val values = js.Array(
        "changeMax" -> changeMax, "changeMin" -> changeMin,
        "priceMax" -> priceMax, "priceMin" -> priceMin,
        "spreadMax" -> spreadMax, "spreadMin" -> spreadMin,
        "volumeMax" -> volumeMax, "volumeMin" -> volumeMin,
        "sortBy" -> sortBy, "reverse" -> reverse,
        "maxResults" -> maxResults
      )

      (for ((name, value) <- values if value.nonEmpty) yield s"$name=$value").mkString("&")
    }
  }

}
