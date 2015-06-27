package com.ldaniels528.javascript.angularjs.core

import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
 * Angular Q Deferral
 * @author lawrence.daniels@gmail.com
 */
trait QDefer[T] extends js.Object {

  def resolve(value: T): Unit = js.native

  def reject(reason: js.Any): Unit = js.native

  def notify(value: T): Unit = js.native

  def promise: QPromise[T] = js.native

}

/**
 * Angular Q Deferral Singleton
 * @author lawrence.daniels@gmail.com
 */
object QDefer {

  implicit class DeferredPromise[T](defer: QDefer[T]) extends scala.concurrent.Promise[T] {
    private var completed = false

    override def future: Future[T] = QPromise.qPromise2Future(defer.promise)

    override def isCompleted: Boolean = completed

    override def tryComplete(result: Try[T]): Boolean = if (isCompleted) false
    else {
      result match {
        case Success(r) =>
          defer.resolve(r)
        case Failure(e) =>
          defer.reject(e.getMessage)
      }

      completed = true
      completed
    }
  }

}

