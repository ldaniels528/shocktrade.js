package com.shocktrade.services.barchart

import com.shocktrade.services.HttpUtil

/**
 * BarChart.com Analyst Rating Chart Service
 * @see http://www.barchart.com/stocks/ratingsimg.php?sym=AAPL
 * @author lawrence.daniels@gmail.com
 */
object BCAnalystRatingsChartService extends HttpUtil {

  /**
   * Retrieves the Analyst Rating/Recommendations for the given stock symbol
   * @param symbol the given stock symbol (e.g. "AAPL")
   * @return an array of bytes representing the binary image data of the chart
   */
  def getRatingsChart(symbol: String): Array[Byte] = {
    getResource(s"http://www.barchart.com/stocks/ratingsimg.php?sym=$symbol")
  }

}
