package com.shocktrade.remote.loader

import scala.concurrent.Future
import scala.scalajs.js

/**
 * Represent an asynchronous Task
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Task(val name: String,
           val args: List[String],
           val correlationID: Option[String],
           val lineNumber: Int,
           val promise: Future[js.Any]) {
  override def toString: String = s"$name(${
    args.map {
      case s if s.startsWith("{") => s
      case s if s.startsWith("\"") => s
      case s => s""""$s""""
    } mkString ","
  })"
}

/**
 * Task Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Task {

  def apply(name: String, args: List[String], correlationID: Option[String], lineNumber: Int, promise: Future[js.Any]): Task = {
    new Task(name, args, correlationID, lineNumber, promise)
  }

  def apply(command: Command, lineNumber: Int, promise: Future[js.Any]): Task = {
    import command._
    new Task(name, args, correlationID, lineNumber, promise)
  }

}
