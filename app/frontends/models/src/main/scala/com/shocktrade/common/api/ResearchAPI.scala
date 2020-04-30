package com.shocktrade.common.api

import com.shocktrade.common.forms.ResearchOptions

import scala.scalajs.js

/**
 * Research API
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ResearchAPI extends BaseAPI {

  def researchURL(options: js.UndefOr[ResearchOptions] = js.undefined): String = {
    val uri = s"$baseURL/api/research/search"
    options.map(o => s"$uri?${o.toQueryString}") getOrElse uri
  }

}
