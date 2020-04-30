package com.shocktrade.common.forms

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

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

  /**
   * Research Options Validations
   * @param options the given [[ContestSearchOptions]]
   */
  final implicit class ResearchOptionsEnrichment(val options: ResearchOptions) extends AnyVal {

    def toQueryString: String = {
      val values = options.asInstanceOf[js.Dictionary[js.UndefOr[Any]]].toJSArray
      (for ((name, value) <- values if value.nonEmpty) yield s"$name=$value").mkString("&")
    }
  }

}
