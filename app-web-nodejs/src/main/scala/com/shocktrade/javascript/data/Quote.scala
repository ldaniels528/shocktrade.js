package com.shocktrade.javascript.data

import org.scalajs.nodejs.mongodb._
import scala.scalajs.js

/**
  * Abstract Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Quote extends js.Object

/**
  * Abstract Quote Companion
  * @author lawrence.daniels@gmail.com
  */
object Quote {

  /**
    * Quote Extensions
    * @param quote the given [[Quote quote]]
    */
  implicit class QuoteExtensions(val quote: Quote) extends AnyVal {

    //def fields() = doc()

  }

}