package com.ldaniels528.javascript.angularjs.core

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
 * Angular Q Service
 * @author lawrence.daniels@gmail.com
 */
trait Q extends js.Object {

  def defer[T](): Defer[T] = js.native

}

/**
 * Angular Q Deferral
 * @author lawrence.daniels@gmail.com
 */
trait Defer[T] extends js.Object {

  def resolve(value: T): Unit = js.native

  def reject(reason: String): Unit = js.native

  def notify(value: T): Unit = js.native

  def promise: Promise[T] = js.native

}

/**
 * Angular Q Deferral Singleton
 * @author lawrence.daniels@gmail.com
 */
object Defer {

  implicit class DeferredPromise[T](defer: Defer[T]) extends scala.concurrent.Promise[T] {
    private var completed = false

    override def future: Future[T] = Promise.qPromise2Future(defer.promise)

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

