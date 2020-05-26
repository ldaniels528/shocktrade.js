package com.shocktrade.webapp.routes.traffic

import scala.scalajs.js

/**
 * Represents traffic data
 */
class TrafficData(val method: js.UndefOr[String],
                  val path: js.UndefOr[String],
                  val query: js.UndefOr[String],
                  val statusCode: js.UndefOr[Int],
                  val statusMessage: js.UndefOr[String],
                  val responseTimeMillis: js.UndefOr[Double],
                  val requestTime: js.UndefOr[js.Date],
                  val creationTime: js.UndefOr[js.Date]) extends js.Object

/**
 * Traffic Data
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object TrafficData {

  def apply(method: js.UndefOr[String] = js.undefined,
            path: js.UndefOr[String] = js.undefined,
            query: js.UndefOr[String] = js.undefined,
            statusCode: js.UndefOr[Int] = js.undefined,
            response: js.UndefOr[String] = js.undefined,
            responseTimeMillis: js.UndefOr[Double] = js.undefined,
            requestTime: js.UndefOr[js.Date] = js.undefined,
            creationTime: js.UndefOr[js.Date] = js.undefined): TrafficData = {
    new TrafficData(method, path, query, statusCode, response, responseTimeMillis, requestTime, creationTime)
  }

}