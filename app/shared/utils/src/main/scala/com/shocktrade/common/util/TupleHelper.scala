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
    def map[R](f: (A, B) => R): R = f(tuple._1, tuple._2)
  }

  /**
   * Tuple3 Enrichment
   * @param tuple the given tuple
   */
  final implicit class Tuple3Enriched[A, B, C](val tuple: (A, B, C)) extends AnyVal {
    def map[R](f: (A, B, C) => R): R = f(tuple._1, tuple._2, tuple._3)
  }

  /**
   * Tuple4 Enrichment
   * @param tuple the given tuple
   */
  final implicit class Tuple4Enriched[A, B, C, D](val tuple: (A, B, C, D)) extends AnyVal {
    def map[R](f: (A, B, C, D) => R): R = f(tuple._1, tuple._2, tuple._3, tuple._4)
  }

  /**
   * Tuple5 Enrichment
   * @param tuple the given tuple
   */
  final implicit class Tuple5Enriched[A, B, C, D, E](val tuple: (A, B, C, D, E)) extends AnyVal {
    def map[R](f: (A, B, C, D, E) => R): R = f(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5)
  }

  /**
   * Tuple6 Enrichment
   * @param tuple the given tuple
   */
  final implicit class Tuple6Enriched[A, B, C, D, E, F](val tuple: (A, B, C, D, E, F)) extends AnyVal {
    def map[R](f: (A, B, C, D, E, F) => R): R = f(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6)
  }

  /**
   * Tuple7 Enrichment
   * @param t the given tuple
   */
  final implicit class Tuple7Enriched[A, B, C, D, E, F, G](val t: (A, B, C, D, E, F, G)) extends AnyVal {
    def map[R](f: (A, B, C, D, E, F, G) => R): R = f(t._1, t._2, t._3, t._4, t._5, t._6, t._7)
  }

  /**
   * Tuple8 Enrichment
   * @param t the given tuple
   */
  final implicit class Tuple8Enriched[A, B, C, D, E, F, G, H](val t: (A, B, C, D, E, F, G, H)) extends AnyVal {
    def map[R](f: (A, B, C, D, E, F, G, H) => R): R = f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8)
  }

  /**
   * Tuple9 Enrichment
   * @param t the given tuple
   */
  final implicit class Tuple9Enriched[A, B, C, D, E, F, G, H, I](val t: (A, B, C, D, E, F, G, H, I)) extends AnyVal {
    def map[R](f: (A, B, C, D, E, F, G, H, I) => R): R = f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9)
  }

}
