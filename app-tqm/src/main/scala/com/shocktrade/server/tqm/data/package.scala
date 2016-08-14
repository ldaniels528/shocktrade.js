package com.shocktrade.server.tqm

import scala.concurrent.{ExecutionContext, Future}

/**
  * data package object
  * @author lawrence.daniels@gmail.com
  */
package object data {

  /**
    * Future Extensions
    * @param futureA the given host [[Future future]]
    */
  implicit class FutureExtensions[A](val futureA: Future[A]) extends AnyVal {

    @inline
    def ++[B](futureB: Future[B])(implicit ec: ExecutionContext) = for {
      a <- futureA
      b <- futureB
    } yield (a, b)

  }

}
