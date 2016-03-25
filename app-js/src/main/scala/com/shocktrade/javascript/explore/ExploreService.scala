package com.shocktrade.javascript.explore

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Explore Service
  * @author lawrence.daniels@gmail.com
  */
class ExploreService($http: Http) extends Service {

  def loadSectorInfo(symbol: String) = {
    required("symbol", symbol)
    $http.get[SectorInfo](s"/api/explore/symbol/$symbol")
  }

  def loadSectors() = $http.get[js.Array[AggregatedData]]("/api/explore/sectors")

  def loadNAICSSectors() = $http.get[js.Array[AggregatedData]]("/api/explore/naics/sectors")

  def loadIndustries(sector: String) = {
    required("sector", sector)
    $http.get[js.Array[AggregatedData]](s"/api/explore/industries${params("sector" -> sector)}")
  }

  def loadSubIndustries(sector: String, industry: String) = {
    required("sector", sector)
    required("industry", industry)
    $http.get[js.Array[AggregatedData]](s"/api/explore/subIndustries${params("sector" -> sector, "industry" -> industry)}")
  }

  def loadIndustryQuotes(sector: String, industry: String, subIndustry: String) = {
    required("sector", sector)
    required("industry", industry)
    required("subIndustry", subIndustry)
    $http.get[js.Array[SectorQuote]](s"/api/explore/quotes${params("sector" -> sector, "industry" -> industry, "subIndustry" -> subIndustry)}")
  }

}

/**
  * Industry Aggregation
  */
@js.native
trait AggregatedData extends js.Object {
  var _id: String
  var total: Int
}

/**
  * Sector/Industry Quote
  */
@js.native
trait SectorQuote extends js.Object

/**
  * Sector Information
  */
@js.native
trait SectorInfo extends js.Object {
  var _id: js.Object
  var symbol: js.UndefOr[String]
  var exchange: js.UndefOr[String]
  var sector: js.UndefOr[String]
  var industry: js.UndefOr[String]
  var subIndustry: js.UndefOr[String]
}

