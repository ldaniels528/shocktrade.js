package com.ldaniels528.javascript.angularjs.core

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

/**
 * Created by ldaniels on 6/23/15.
 */
trait HttpConfig extends js.Object {
  var method: String = js.native
  var url: String = js.native
  var params: js.Dictionary[js.Any] = js.native
  var data: js.Any = js.native
  var headers: js.Dictionary[js.Any] = js.native
  var xsrfHeaderName: String = js.native
  var xsrfCookieName: String = js.native
  var transformResponse: js.Array[js.Function2[js.Any, js.Any, js.Any]] = js.native
  var transformRequest: js.Array[js.Function2[js.Any, js.Any, js.Any]] = js.native
  var cache: js.Any = js.native
  var withCredentials: Boolean = js.native
  var timeout: js.Any = js.native
  var responseType: String = js.native
}

object HttpConfig {

  def apply[A](params: (String, A)*): HttpConfig = literal(params = js.Dictionary(params: _*)).asInstanceOf[HttpConfig]

  def apply(method: String = null,
            url: String = null,
            params: js.Dictionary[js.Any] = null,
            data: js.Any = null,
            headers: js.Dictionary[js.Any] = null,
            xsrfHeaderName: String = null,
            xsrfCookieName: String = null,
            transformResponse: js.Array[js.Function2[js.Any, js.Any, js.Any]] = null,
            transformRequest: js.Array[js.Function2[js.Any, js.Any, js.Any]] = null,
            cache: js.Any = null,
            withCredentials: Boolean = false,
            timeout: js.Any = null,
            responseType: String = null): HttpConfig = {
    literal(method = method, url = url, params = params, data = data, headers = headers,
      xsrfCookieName = xsrfCookieName, xsrfHeaderName = xsrfHeaderName,
      transformRequest = transformRequest, transformResponse = transformResponse,
      cache = cache, withCredentials = withCredentials, timeout = timeout,
      responseType = responseType).asInstanceOf[HttpConfig]
  }

}
