package com.shocktrade.ingestion.daemons.onetime

import scala.scalajs.js

/**
 * Represents Wikipedia Company Data
 * @example {{{
 * "MMM","3M Company","Industrials","Industrial Conglomerates","St. Paul, Minnesota","1976-08-09",0000066740,1902
 * }}}
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class WikipediaCompanyData(val symbol: js.UndefOr[String],
                           val name: js.UndefOr[String],
                           val sector: js.UndefOr[String],
                           val industry: js.UndefOr[String],
                           val cityState: js.UndefOr[String],
                           val initialReportingDate: js.UndefOr[String],
                           val cikNumber: js.UndefOr[String],
                           val yearFounded: js.UndefOr[String]) extends js.Object