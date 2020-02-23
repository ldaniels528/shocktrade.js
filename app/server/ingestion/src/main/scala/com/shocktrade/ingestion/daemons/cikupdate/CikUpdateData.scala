package com.shocktrade.ingestion.daemons.cikupdate

import scala.scalajs.js

class CikUpdateData(val symbol: js.UndefOr[String],
                    val exchange: js.UndefOr[String],
                    val cikNumber: js.UndefOr[String],
                    val companyName: js.UndefOr[String],
                    val mailingAddress: js.UndefOr[String]) extends js.Object