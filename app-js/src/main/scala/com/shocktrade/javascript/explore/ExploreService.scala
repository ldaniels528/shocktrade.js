package com.shocktrade.javascript.explore

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Http
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.explore.ExploreService._

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
 * Explore Service
 * @author lawrence.daniels@gmail.com
 */
object ExploreService {

  /**
   * Industry Aggregation
   */
  trait AggregatedData extends js.Object {
    var _id: String = js.native
    var total: Int = js.native
  }

  /**
   * Sector/Industry Quote
   */
  trait SectorQuote extends js.Object

  /**
   * Sector Information
   */
  trait SectorInfo extends js.Object {
    var _id: js.Object = js.native
    var exchange: String = js.native
    var sector: String = js.native
    var industry: String = js.native
    var subIndustry: String = js.native
    var symbol: String = js.native
  }

}
