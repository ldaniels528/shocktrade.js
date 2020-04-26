package com.shocktrade.robots

import io.scalajs.npm.express.Response

import scala.scalajs.js

package object routes {

  type NextFunction = js.Function0[Unit]

  /**
   * Response Extensions
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  implicit class ResponseExtensions(val response: Response) extends AnyVal {

    def showException(e: Throwable): response.type = {
      e.printStackTrace()
      response
    }

  }

}
