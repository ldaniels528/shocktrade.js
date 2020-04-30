package com.shocktrade.common.api

/**
 * Remote Activity API
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait RemoteEventAPI extends BaseAPI {

  def relayEventURL = s"$baseURL/api/events/relay"

}
