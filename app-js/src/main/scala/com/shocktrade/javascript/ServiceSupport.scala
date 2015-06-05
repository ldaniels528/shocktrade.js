package com.shocktrade.javascript

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.util.{Failure, Success, Try}

/**
 * Service Support
 * @author lawrence.daniels@gmail.com
 */
trait ServiceSupport {

  protected def flatten[T](future: Future[Try[T]]): Future[T] = future flatMap {
    case Success(s) => Future.successful(s)
    case Failure(f) => Future.failed(f)
  }

}
