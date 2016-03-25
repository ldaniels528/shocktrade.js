package com.shocktrade.services.util

/**
 * Regular Expressions-based Date Parser
 * @author lawrence.daniels@gmail.com
 */
object RegexDateParser {
  import java.text.SimpleDateFormat
  import java.util.{Calendar, Date}

  private[this] val MMM_DD = "(\\S{3})-(0?[1-9]|[12][0-9]|3[01])"
  private[this] val DD_MMM_YYYY = "(0?[1-9]|[12][0-9]|3[01])-(\\S{3})-((19|20)\\d{2})"
  private[this] val MM_DD_YYYY = "(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])-((19|20)\\d{2})"
  private[this] val MM_$DD_$YYYY = "(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d{2})"
  private[this] val YYYY_MM_DD = "((19|20)\\d{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])"
  private[this] val YYYY_$MM_$DD = "((19|20)\\d{2})/(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])"
  private[this] val TIMESTAMP = YYYY_MM_DD + " \\d{2}:\\d{2}:\\d{2}"

  def parseDate(dateString: String): Date = {
    dateString match {
      case s if s.matches(DD_MMM_YYYY) => new SimpleDateFormat("dd-MMM-yy").parse(s)
      case s if s.matches(MMM_DD) => {
        // get the current year
        val cal1 = Calendar.getInstance()
        val year = cal1.get(Calendar.YEAR)

        // create the date
        val cal2 = Calendar.getInstance();
        cal2.setTime(new SimpleDateFormat("MMM dd").parse(s))
        cal2.set(Calendar.YEAR, year)
        if (cal2.getTime().after(cal1.getTime())) {
          cal2.add(Calendar.YEAR, -1)
        }
        cal2.getTime()
      }
      case s if s.matches(MM_DD_YYYY) => new SimpleDateFormat("MM-dd-yyyy").parse(s)
      case s if s.matches(MM_$DD_$YYYY) => new SimpleDateFormat("MM/dd/yyyy").parse(s)
      case s if s.matches(YYYY_MM_DD) => new SimpleDateFormat("yyyy-MM-dd").parse(s)
      case s if s.matches(YYYY_$MM_$DD) => new SimpleDateFormat("yyyy/MM/dd").parse(s)
      case s if s.matches(TIMESTAMP) => new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(s)
      case value =>
        throw new IllegalArgumentException(s"Unrecognized date format - '$value'")
    }
  }

  /**
   * Indicates whether the given string is a valid time string
   * @param timeString the given time string (e.g. "10:12am")
   * @return true, if the given string is a valid time string
   */
  def isTime(s: String) = s.toUpperCase.matches("\\d{1,2}:\\d{2}(:\\d{2})?(A|P|AM|PM)")

  /**
   * Indicates whether the given string is a valid time string
   * @param timeString the given time string (e.g. "10:12am")
   * @return true, if the given string is a valid time string
   */
  def isTime2(timeString: String) = timeString.matches("([\\d]{1,2}:[\\d]{1,2}|[\\d]{1,2}:[\\d]{1,2}[aApP][mM])")

}