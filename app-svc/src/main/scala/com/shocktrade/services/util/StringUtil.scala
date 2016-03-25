package com.shocktrade.services.util

/**
 * ShockTrade String Utilities
 * @author lawrence.daniels@gmail.com
 */
object StringUtil {

  /**
   * Indicates whether the given string value is <tt>null</tt> or empty
   * @return true, if the given string value is <tt>null</tt> or empty
   */
  def isBlank(value: String): Boolean = (value == null) || (value.trim.length() == 0)

  /**
   * Indicates whether the given string value is <tt>null</tt> or empty
   * @param value the given string value
   * @return true, if the given string value is <tt>null</tt> or empty
   */
  def nonBlank(value: String): Boolean = (value != null) && (value.trim.length() > 0)

  /**
   * Enriched String
   * @author lawrence.daniels@gmail.com
   */
  implicit class StringEnrichment(value: String) {

    /**
     * Indicates whether the given string value is <tt>null</tt> or empty
     * @return true, if the given string value is <tt>null</tt> or empty
     */
    def isBlank: Boolean = value.trim.isEmpty

    /**
     * Indicates whether the given string value is <tt>null</tt> or empty
     * @return true, if the given string value is <tt>null</tt> or empty
     */
    def nonBlank: Boolean = value.trim.nonEmpty

    /**
     * Indicates whether the given string value represents a numeric value (e.g. "123.46")
     * @return true, if the given string value represents a numeric value
     */
    def isNumber: Boolean = isInteger || isDecimal

    /**
     * Indicates whether the given string value represents an integer
     * @return true, if the given string value represents an integer
     */
    def isInteger: Boolean = value.matches("\\d+")

    /**
     * Indicates whether the given string value represents an integer
     * @return true, if the given string value represents an integer
     */
    def isDecimal: Boolean = value.matches("\\d+(\\.\\d+)?") || value.matches("\\.\\d+")

  }

}