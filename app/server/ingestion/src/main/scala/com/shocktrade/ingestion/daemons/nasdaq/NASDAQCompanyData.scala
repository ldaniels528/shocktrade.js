package com.shocktrade.ingestion.daemons.nasdaq

import scala.scalajs.js

/**
 * Represents NASDAQ Company List Data
 */
class NASDAQCompanyData(val symbol: js.UndefOr[String],
                        val exchange: js.UndefOr[String],
                        val companyName: js.UndefOr[String],
                        val lastTrade: js.UndefOr[Double],
                        val marketCap: js.UndefOr[Double],
                        val ADRTSO: js.UndefOr[String],
                        val IPOyear: js.UndefOr[Int],
                        val sector: js.UndefOr[String],
                        val industry: js.UndefOr[String],
                        val summary: js.UndefOr[String],
                        val quote: js.UndefOr[String]) extends js.Object