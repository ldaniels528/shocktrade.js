package com.shocktrade.stockguru.explore

import com.shocktrade.common.models.quote.SectorInfoQuote
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http
import org.scalajs.dom.browser.encodeURI

import scala.scalajs.js

/**
  * Explore Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ExploreService($http: Http) extends Service {

  def loadSectorInfo(symbol: String) = $http.get[SectorInfoQuote](s"/api/explore/symbol/$symbol")

  def loadSectors() = $http.get[js.Array[AggregatedSectorData]]("/api/explore/sectors")

  def loadNAICSSectors() = $http.get[js.Array[AggregatedSectorData]]("/api/explore/naics/sectors")

  def loadIndustries(sector: String) = {
    $http.get[js.Array[AggregatedSectorData]](s"/api/explore/industries?sector=${encodeURI(sector)}")
  }

  def loadSubIndustries(sector: String, industry: String) = {
    $http.get[js.Array[AggregatedSectorData]](s"/api/explore/subIndustries?sector=${encodeURI(sector)}&industry=${encodeURI(industry)}")
  }

  def loadIndustryQuotes(sector: String, industry: String, subIndustry: String) = {
    $http.get[js.Array[SectorQuote]](s"/api/explore/quotes?sector=${encodeURI(sector)}&industry=${encodeURI(industry)}&subIndustry=${encodeURI(subIndustry)}")
  }

}

/**
  * Aggregated Sector/Industry data
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait AggregatedSectorData extends js.Object {
  var _id: String = js.native
  var total: Int = js.native
}

/**
  * Sector/Industry Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait SectorQuote extends js.Object

