package com.shocktrade.remote.proxies

import com.shocktrade.common.api.ResearchAPI
import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.ResearchQuote

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Research Proxy
 * @param host the given host
 * @param port the given port
 */
class ResearchProxy(host: String, port: Int)(implicit ec: ExecutionContext) extends Proxy with ResearchAPI  {
  override val baseURL = s"http://$host:$port"

  def research(options: ResearchOptions): Future[js.Array[ResearchQuote]] = get(researchURL(options))

}
