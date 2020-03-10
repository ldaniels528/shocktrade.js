package com.shocktrade.client.discover

import com.shocktrade.client.discover.ExploreService._
import com.shocktrade.common.models.quote.{ResearchQuote, SectorInfoQuote}
import io.scalajs.dom.html.browser.encodeURI
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * Explore Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ExploreService($http: Http) extends Service {

  def loadSectorInfo(symbol: String): js.Promise[HttpResponse[SectorInfoQuote]] = {
    $http.get[SectorInfoQuote](s"/api/explore/symbol/$symbol")
  }

  def loadSectors(): js.Promise[HttpResponse[js.Array[AggregatedSectorData]]] = {
    $http.get[js.Array[AggregatedSectorData]]("/api/explore/sectors")
  }

  def loadNAICSSectors(): js.Promise[HttpResponse[js.Array[AggregatedSectorData]]] = {
    $http.get[js.Array[AggregatedSectorData]]("/api/explore/naics/sectors")
  }

  def loadIndustries(sector: String): js.Promise[HttpResponse[js.Array[AggregatedSectorData]]] = {
    $http.get[js.Array[AggregatedSectorData]](s"/api/explore/industries?sector=${sector.encode}")
  }

  def loadIndustryQuotes(sector: String, industry: String): js.Promise[HttpResponse[js.Array[ResearchQuote]]] = {
    $http.get[js.Array[ResearchQuote]](s"/api/explore/quotes?sector=${sector.encode}&industry=${industry.encode}")
  }

  def loadSubIndustries(sector: String, industry: String): js.Promise[HttpResponse[js.Array[AggregatedSectorData]]] = {
    $http.get[js.Array[AggregatedSectorData]](s"/api/explore/subIndustries?sector=${sector.encode}&industry=${industry.encode}")
  }

  def loadSubIndustryQuotes(sector: String, industry: String, subIndustry: String): js.Promise[HttpResponse[js.Array[ResearchQuote]]] = {
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
    def encode: String = encodeURI(s).replaceAllLiterally("&", "%26")

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

