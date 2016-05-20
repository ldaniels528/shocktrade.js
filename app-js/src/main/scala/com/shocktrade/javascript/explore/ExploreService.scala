package com.shocktrade.javascript.explore

import com.github.ldaniels528.meansjs.angularjs.Service
import com.github.ldaniels528.meansjs.angularjs.http.{Http, HttpConfig}

import scala.scalajs.js

/**
  * Explore Service
  * @author lawrence.daniels@gmail.com
  */
class ExploreService($http: Http) extends Service {

  def loadSectorInfo(symbol: String) = {
    $http.get[SectorInfo](s"/api/explore/symbol/$symbol")
  }

  def loadSectors() = $http.get[js.Array[AggregatedData]]("/api/explore/sectors")

  def loadNAICSSectors() = $http.get[js.Array[AggregatedData]]("/api/explore/naics/sectors")

  def loadIndustries(sector: String) = {
    $http[js.Array[AggregatedData]](HttpConfig(
      method = "GET",
      url = "/api/explore/industries",
      params = js.Dictionary("sector" -> sector)
    ))
  }

  def loadSubIndustries(sector: String, industry: String) = {
    $http[js.Array[AggregatedData]](HttpConfig(
      method = "GET",
      url = "/api/explore/subIndustries",
      params = js.Dictionary("sector" -> sector, "industry" -> industry)
    ))
  }

  def loadIndustryQuotes(sector: String, industry: String, subIndustry: String) = {
    $http[js.Array[SectorQuote]](HttpConfig(
      method = "GET",
      url = "/api/explore/quotes",
      params = js.Dictionary("sector" -> sector, "industry" -> industry, "subIndustry" -> subIndustry)
    ))
  }

}

/**
  * Industry Aggregation
  */
@js.native
trait AggregatedData extends js.Object {
  var _id: String = js.native
  var total: Int = js.native
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
  var _id: js.Object = js.native
  var symbol: js.UndefOr[String] = js.native
  var exchange: js.UndefOr[String] = js.native
  var sector: js.UndefOr[String] = js.native
  var industry: js.UndefOr[String] = js.native
  var subIndustry: js.UndefOr[String] = js.native
}

