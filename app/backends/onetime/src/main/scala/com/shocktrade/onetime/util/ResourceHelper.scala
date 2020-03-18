package com.shocktrade.onetime.util

import scala.language.reflectiveCalls

/**
 * Resource Helper
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ResourceHelper {

  type Closeable = {def close(): Unit}

  /**
   * AutoClose
   * @param closeable the given [[Closeable]]
   */
  final implicit class AutoClose[T <: Closeable](val closeable: T) extends AnyVal {

    @inline def as[A](f: T => A): A = try f(closeable) finally closeable.close()

  }

}
