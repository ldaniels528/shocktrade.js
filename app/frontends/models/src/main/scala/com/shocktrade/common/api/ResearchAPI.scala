package com.shocktrade.common.api

import com.shocktrade.common.forms.ResearchOptions

/**
 * Research API
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ResearchAPI extends BaseAPI {

  val researchURLPrefix = "/api/research/search"

  def researchURL(options: ResearchOptions) = s"$baseURL$researchURLPrefix?${options.toQueryString}"

}
