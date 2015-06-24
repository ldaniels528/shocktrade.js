package com.ldaniels528.javascript.angularjs.core

import scala.scalajs.js

/**
 * Angular.js Logging Service ($log)
 * @author lawrence.daniels@gmail.com
 * @see https://docs.angularjs.org/api/ng/service/$log
 */
trait Log {

  def log(message: js.Any)

  def debug(message: js.Any)

  def error(message: js.Any)

  def info(message: js.Any)

  def warn(message: js.Any)

}
