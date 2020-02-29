package com.shocktrade.ingestion.daemons.eoddata

import scala.scalajs.js

/**
 * Represents an EOD data record
 */
class EodDataRecord(val symbol: js.UndefOr[String],
                    val exchange: js.UndefOr[String],
                    val name: js.UndefOr[String],
                    val high: js.UndefOr[Double],
                    val low: js.UndefOr[Double],
                    val close: js.UndefOr[Double],
                    val volume: js.UndefOr[Double],
                    val change: js.UndefOr[Double],
                    val changePct: js.UndefOr[Double]) extends js.Object