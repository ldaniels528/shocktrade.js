package com.shocktrade.javascript

import com.github.ldaniels528.scalascript.Scope
import com.github.ldaniels528.scalascript.core.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Global Loading Control
  * @author lawrence.daniels@gmail.com
  */
trait GlobalLoading {

  def syncLoading[T]($scope: Scope, $timeout: Timeout)(block: => T): T = {
    val promise = $scope.dynamic.startLoading()
    try block finally $timeout(() => $scope.dynamic.stopLoading(promise), 500)
  }

  def asyncLoading[T]($scope: Scope)(task: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val promise = $scope.dynamic.startLoading()
    task onComplete {
      case Success(_) => $scope.dynamic.stopLoading(promise)
      case Failure(_) => $scope.dynamic.stopLoading(promise)
    }
    task
  }

}
