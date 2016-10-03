package com.shocktrade.stockguru.explore

import com.shocktrade.common.models.quote.{ResearchQuote, SectorInfoQuote}
import com.shocktrade.stockguru.explore.ExploreService._
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
    $http.get[js.Array[AggregatedSectorData]](s"/api/explore/industries?sector=${sector.encode}")
  }

  def loadIndustryQuotes(sector: String, industry: String) = {
    $http.get[js.Array[ResearchQuote]](s"/api/explore/quotes?sector=${sector.encode}&industry=${industry.encode}")
  }

  def loadSubIndustries(sector: String, industry: String) = {
    $http.get[js.Array[AggregatedSectorData]](s"/api/explore/subIndustries?sector=${sector.encode}&industry=${industry.encode}")
  }

  def loadSubIndustryQuotes(sector: String, industry: String, subIndustry: String) = {
    $http.get[js.Array[ResearchQuote]](s"/api/explore/quotes?sector=${sector.encode}&industry=${industry.encode}&subIndustry=${subIndustry.encode}")
  }

}

/**
  * Explore Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ExploreService {

  /**
    * URL encodes the given string; replacing the amperstand (&) in the string identifier to fix URL encoding problems
    * @param s the given string identifier
    */
  implicit class URLStringFix(val s: String) extends AnyVal {

    @inline
    def encode = encodeURI(s).replaceAllLiterally("&", "%26")

  }

  /**
    * Aggregated Sector/Industry data
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @js.native
  trait AggregatedSectorData extends js.Object {
    val _id: String = js.native
    val total: Int = js.native
  }

}

