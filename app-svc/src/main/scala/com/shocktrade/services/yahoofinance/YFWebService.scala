package com.shocktrade.services.yahoofinance

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import com.shocktrade.services.TextParsing
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

/**
 * Yahoo! Finance Web Service Trait
 * @author lawrence.daniels@gmail.com
 */
trait YFWebService extends TextParsing {
  protected def asTime(s: String): Option[Date] = {
    import Calendar._

    // there are 2 known formats:
    //	"MMM dd, hh:mma z" (e.g. "Jan 10, 4:00PM EST")
    //	"hh:mma z" => (e.g. "4:00PM EST")
    val cal = Calendar.getInstance()
    val (year, month, day) = (cal.get(YEAR), cal.get(MONTH), cal.get(DAY_OF_MONTH))

    def format1(s: String) = {
      Try(new SimpleDateFormat("hh:mma z").parse(s)) match {
        case Success(date) => Some(new DateTime(date).withDate(year, month + 1, day).toDate)
        case Failure(e) => None
      }
    }

    def format2(s: String) = {
      Try(new SimpleDateFormat("MMM dd, hh:mma z").parse(s)) match {
        case Success(date) => Some(new DateTime(date).withYear(year).toDate)
        case Failure(e) => None
      }
    }

    // find the qualifying format
    Seq(format1 _, format2 _).foldLeft[Option[Date]](None)((res, fx) => if (res.isEmpty) fx(s) else res)
  }

  /**
   * Attempts to retrieve the first and second items from the sequence
   * and return them as a tuple
   */
  protected def numberTuple(value: Option[String]): (Option[Double], Option[Double]) = {
    val (s1, s2) = tuplize(value)
    (s1 flatMap asNumber, s2 flatMap asNumber)
  }
  
  /**
   * Attempts to retrieve the first and second items from the sequence
   * and return them as a tuple
   */
  protected def numberTuple(value: Option[String], regex: String): (Option[Double], Option[Double]) = {
    val (s1, s2) = tuple(value, regex)
    (s1 flatMap asNumber, s2 flatMap asNumber)
  }

  /**
   * Attempts to retrieve the first and second items from the sequence
   * and return them as a tuple
   */
  protected def tuplize(value: Option[String]): (Option[String], Option[String]) = {
    value match {
      case None => (None, None)
      case Some(item) =>
        val seq = DECIMAL_r.findAllIn(item).toSeq
        seq match {
          case Seq(first, second, _*) => (Some(first), Some(second))
          case Seq(first, _*) => (Some(first), None)
          case _ => (None, None)
        }
    }
  }

  /**
   * Attempts to retrieve the first and second items from the sequence
   * and return them as a tuple
   */
  protected def tuple(value: Option[String], regex: String) = {
    value match {
      case Some(s) => s.split(regex) match {
        case Array(a, b, _*) => (Some(a), Some(b))
        case Array(a, _*) => (Some(a), None)
        case _ => (None, None)
      }
      case None => (None, None)
    }
  }

  /**
   * Enriched Map
   * @author lawrence.daniels@gmail.com
   */
  implicit class EnrichedMap(m: Map[String, String]) {

    /**
     * Finds the key that starts with the prefix
     */
    def ~>(prefix: String): Option[String] = m.find(_._1.startsWith(prefix)) flatMap (kv => cleanse(kv._2))

  }

}