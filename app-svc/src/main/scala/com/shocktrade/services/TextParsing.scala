package com.shocktrade.services

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.{ Try, Success, Failure }

/**
 * Text Parsing Trait
 * @author lawrence.daniels@gmail.com
 */
trait TextParsing {
  protected val DECIMAL_r = "(\\d+\\.\\d*)".r
  protected def cleanse(s: String): Option[String] = if (s == null || s.trim.isEmpty || s.trim == "N/A") None else Some(s)
  protected def asDate(s: String, format: String): Option[Date] = {
    val sdf = new SimpleDateFormat(format)
    Try { cleanse(s) map sdf.parse } match {
      case Success(date) => date
      case Failure(e) => None
    }
  }

  protected def asInt(s: String) = asNumber(s) map (_.toInt)

  protected def asLong(s: String) = asNumber(s) map (_.toLong)

  protected def asNumber(value: String): Option[Double] = {
    Try(for {
      s <- cleanse(value)
      t = s.replaceAll(",", "")
      u = t.last match {
        case 'K' => t.dropRight(1).toDouble * 1.0e3
        case 'M' => t.dropRight(1).toDouble * 1.0e6
        case 'B' => t.dropRight(1).toDouble * 1.0e9
        case 'T' => t.dropRight(1).toDouble * 1.0e12
        case '%' => t.dropRight(1).toDouble
        case _ => t.toDouble
      }
    } yield u) match {
      case Success(v) => v flatMap(n => if(n.isNaN) None else Some(n))
      case Failure(e) => None
    }
  }

  /**
   * Extracts a string from the array for the given index
   */
  protected def extract(pcs: Array[String], index: Int): Option[String] = {
    if (index < pcs.length) Some(pcs(index)) else None
  }

  protected def toClose(prevClose: Option[Double], change: Option[Double]): Option[Double] = {
    for { pc <- prevClose; c <- change } yield pc + c
  }

  protected def toSpread(high: Option[Double], low: Option[Double]): Option[Double] = {
    for { h <- high; l <- low } yield if (h != 0.0d) 100.0d * ((h - l) / h) else 0.0d
  }

}
