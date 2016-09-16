package com.shocktrade.webapp

import org.scalajs.nodejs.express.{Request, Response}

import scala.scalajs.js

/**
  * Routes package object
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
package object routes {

  type NextFunction = js.Function0[Unit]

  /**
    * Request Extensions
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  implicit class RequestExtensions(val request: Request) extends AnyVal {

    def getMaxResults(default: Int = 20) = request.query.get("maxResults") map (_.toInt) getOrElse default

    def getSymbol = request.params("symbol").toUpperCase()

  }

  /**
    * Parameter Extensions
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  implicit class ParameterExtensions(val params: js.Dictionary[String]) extends AnyVal {

    @inline
    def extractParams(names: String*) = {
      val values = names.map(params.get)
      if (values.forall(_.isDefined)) Some(values.flatten) else None
    }
  }

  /**
    * Response Extensions
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  implicit class ResponseExtensions(val response: Response) extends AnyVal {

    @inline
    def missingParams(params: String*) = {
      val message = s"Bad Request: ${params.mkString(" and ")} ${if (params.length == 1) "is" else "are"} required"
      response.status(400).send(message)
    }

    private def asString(value: js.Any): String = value match {
      case v if v == null => ""
      case v if js.typeOf(v) == "string" => s""""${v.toString}""""
      case v => v.toString
    }

  }

}
