package com.shocktrade.common.util

/**
 * Tuple Helper
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object TupleHelper {

  /**
   * Tuple2 Enrichment
   * @param tuple the given tuple
   */
  final implicit class Tuple2Enriched[A, B](val tuple: (A, B)) extends AnyVal {
    def into[R](f: (A, B) => R): R = f(tuple._1, tuple._2)
  }

  /**
   * Tuple3 Enrichment
   * @param tuple the given tuple
   */
  final implicit class Tuple3Enriched[A, B, C](val tuple: (A, B, C)) extends AnyVal {
    def into[R](f: (A, B, C) => R): R = f(tuple._1, tuple._2, tuple._3)
  }

  /**
   * Tuple4 Enrichment
   * @param tuple the given tuple
   */
  final implicit class Tuple4Enriched[A, B, C, D](val tuple: (A, B, C, D)) extends AnyVal {
    def into[R](f: (A, B, C, D) => R): R = f(tuple._1, tuple._2, tuple._3, tuple._4)
  }

}
