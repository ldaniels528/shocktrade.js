package com.shocktrade.core

/**
  * String Helper
  * @author lawrence.daniels@gmail.com
  */
object StringHelper {

  /**
    * String Extensions
    * @author lawrence.daniels@gmail.com
    */
  implicit class StringExtensions(val string: String) extends AnyVal {

    def isBlank = string.trim.isEmpty

    def nonBlank = string.trim.nonEmpty

    def isValidEmail = string.contains("@") // TODO

    def nonValidEmail = !string.isValidEmail

    def indexOfOpt(s: String) = string.indexOf(s) match {
      case -1 => None
      case index => Some(index)
    }

    def indexOfOpt(s: String, fromIndex: Int) = string.indexOf(s, fromIndex) match {
      case -1 => None
      case index => Some(index)
    }

  }

}
