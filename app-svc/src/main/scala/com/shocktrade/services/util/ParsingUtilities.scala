package com.shocktrade.services.util

/**
 * Parsing Utilities
 * @author lawrence.daniels@gmail.com
 */
object ParsingUtilities {

  /**
   * String Extensions (Type A)
   * @author lawrence.daniels@gmail.com
   */
  implicit class ExtStringA[T <: { def indexOf(s: String, p: Int): Int }](src: T) {

    def findMatches(beginSeq: String, endSeq: String, pos: Int = -1): List[(Int, Int)] = {
      src indexOptionOf (beginSeq, endSeq, pos) match {
        case Some((start, end)) => (start, end) :: findMatches(beginSeq, endSeq, end)
        case None => Nil
      }
    }

    /**
     * <pre>
     * 	<tag>content</tag>
     * </pre>
     */
    def tagContent(tag: String, pos: Int = -1) = {
      (src.indexOptionOf(s"<$tag", ">", pos), src.optionOf(s"</$tag>", pos)) match {
        case (Some((a0, a1)), Some(a2)) => Some((a1, a2))
        case _ => None
      }
    }

    def indexOptionOf(beginSeq: String, endSeq: String, pos: Int = -1): Option[(Int, Int)] = {
      val p0 = src.indexOf(beginSeq, pos)
      if (p0 == -1) None
      else {
        // find the terminating sequence
        val p1 = src.indexOf(endSeq, p0 + beginSeq.length)
        if (p1 == -1) None else Some(p0, p1 + endSeq.length)
      }
    }

    def optionOf(s: String, pos: Int = -1): Option[Int] = {
      val index = src.indexOf(s, pos)
      if (index != -1) Some(index) else None
    }

    def findNestedMatches(searchSeq: String, beginSeq: String, endSeq: String, pos: Int = -1): List[(Int, Int)] = {
      src nestedIndexOptionOf (searchSeq, beginSeq, endSeq, pos) match {
        case Some((start, end)) => (start, end) :: findNestedMatches(searchSeq, beginSeq, endSeq, end)
        case None => Nil
      }
    }

    def nestedIndexOptionOf(searchSeq: String, beginSeq: String, endSeq: String, pos: Int = -1): Option[(Int, Int)] = {
      val p0 = src.indexOf(searchSeq, pos)
      if (p0 == -1) None
      else {
        // find the terminating sequence
        val p1 = src.indexOf(endSeq, p0 + searchSeq.length)
        if (p1 == -1) None else findNestedSpan(searchSeq, beginSeq, endSeq, p0, p1 + endSeq.length)
      }
    }

    def findNestedSpan(searchSeq: String, beginSeq: String, endSeq: String, p0: Int, p1: Int, level: Int = 0): Option[(Int, Int)] = {
      // are there nested copies of the starting sequence?
      val d0 = src.indexOf(beginSeq, p0 + searchSeq.length)
      if (d0 == -1 || d0 > p1) Some(p0, p1)
      else {
        // find a new ending sequence
        val d1 = src.indexOf(endSeq, p1)
        if (d1 == -1) Some(p0, p1)
        else findNestedSpan(searchSeq, beginSeq, endSeq, p0, d1 + endSeq.length, level + 1)
      }
    }

  }

  /**
   * String Extensions (Type B)
   * @author lawrence.daniels@gmail.com
   */
  implicit class ExtStringB[T <: { def lastIndexOf(s: String): Int }](src: T) {

    def optionOfLastIndexOf(s: String): Option[Int] = {
      val index = src.lastIndexOf(s)
      if (index != -1) Some(index) else None
    }
  }

  /**
   * String Extensions (Type C)
   * @author lawrence.daniels@gmail.com
   */
  implicit class ExtStringC(src: StringBuilder) {

    def extractTag(locatorTag: String, startTag: String, endTag: String, pos: Int = 0) = {
      src.indexOptionOf(locatorTag, endTag, pos) match {
        case Some((start, end)) =>
          // find the end of the span
          var nextStart = src.optionOf(startTag, end)
          var nextEnd = src.optionOf(endTag, end)

          Some(src.substring(start, end + endTag.length))
        case None => None
      }
    }

    def replaceTag(startTag: String, endTag: String, replacement: String, initPos: Int = 0) {
      var quit = false
      while (!quit && src.indexOf(startTag) != -1) {
        src.indexOptionOf(startTag, endTag, initPos) match {
          case Some((p0, p1)) => src.replace(p0, p1, replacement)
          case None => quit = true
        }
      }
    }
  }

}