package com.shocktrade.remote.proxies

import io.scalajs.JSON
import io.scalajs.nodejs.http.IncomingMessage
import io.scalajs.nodejs.util.Util
import io.scalajs.npm.request.{Request, RequestOptions}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Represents a Proxy; a REST-based ShockTrade client
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait Proxy {

  private def handler[R <: js.Any](input: (IncomingMessage, R)): R = input match {
    case (response, _) if response.statusCode >= 400 => throw js.JavaScriptException(response.statusMessage)
    case (_, body) if js.Array.isArray(body) | Util.isObject(body) => body.asInstanceOf[R]
    case (_, body) if Util.isString(body) =>JSON.parseAs[R](body.toString)
    case (_, body) => body.asInstanceOf[R]
  }

  protected def delete[R <: js.Any](url: String)(implicit ec: ExecutionContext): Future[R] = {
    Request.deleteFuture[R](new RequestOptions(url = url)) map handler[R]
  }

  protected def get[R <: js.Any](url: String)(implicit ec: ExecutionContext): Future[R] = {
    Request.getFuture[R](new RequestOptions(url = url)) map handler[R]
  }

  protected def post[R <: js.Any](url: String)(implicit ec: ExecutionContext): Future[R] = {
    Request.postFuture[R](new RequestOptions(url = url)) map handler[R]
  }

  protected def post[R <: js.Any](url: String, data: js.Any)(implicit ec: ExecutionContext): Future[R] = {
    Request.postFuture[R](new RequestOptions(url = url, body = data, json = true)) map handler[R]
  }

  protected def put[R <: js.Any](url: String)(implicit ec: ExecutionContext): Future[R] = {
    Request.putFuture[R](new RequestOptions(url = url)) map handler[R]
  }

  protected def put[R <: js.Any](url: String, data: js.Any)(implicit ec: ExecutionContext): Future[R] = {
    Request.putFuture[R](new RequestOptions(url = url, body = data, json = true)) map handler[R]
  }

}
