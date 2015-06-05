package com.shocktrade.controllers

/**
 * Represents parsing capabilities
 * @author lawrence.daniels@gmail.com
 */
trait ParsingCapabilities {

  type IndexedString = {
    def indexOf(s: String): Int
    def indexOf(s: String, start: Int): Int
    def substring(start: Int, end: Int): String
  }

  /**
   * Index-Of Options
   * @author lawrence.daniels@gmail.com
   */
  implicit class indexOptions[T <: IndexedString](src: T) {

    def contents(startSeq: String, endSeq: String, pos:Int = 0): Option[(String, Int, Int)] = {
      val start = src.indexOf(startSeq, pos)
      val end = src.indexOf(endSeq, start)
      val limit = end + endSeq.length
      if (start != -1 && end > start) Some((src.substring(start, limit), start, limit)) else None
    }

    def indexOptionOf(partial: String) = {
      src.indexOf(partial) match {
        case -1 => None
        case n => Some(n)
      }
    }

    def indexOptionOf(partial: String, start: Int) = {
      src.indexOf(partial, start) match {
        case -1 => None
        case n => Some(n)
      }
    }

  }

}