package com.shocktrade.javascript.explore

import com.github.ldaniels528.scalascript.core.{Location, Timeout}
import com.github.ldaniels528.scalascript.extensions.{AnchorScroll, Cookies, Toaster}
import com.github.ldaniels528.scalascript.{Controller, Scope, injected, scoped}
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.explore.ExploreController._
import com.shocktrade.javascript.explore.ExploreService.{SectorInfo, SectorQuote}
import org.scalajs.dom.console

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Explore Controller
 * @author lawrence.daniels@gmail.com
 */
class ExploreController($scope: ExploreScope, $anchorScroll: AnchorScroll, $cookies: Cookies,
                        $location: Location, $routeParams: ExploreRouteParams, $timeout: Timeout, toaster: Toaster,
                        @injected("ExploreService") exploreService: ExploreService)
  extends Controller {

  // initialize scope variables
  $scope.sectors = emptyArray[Sector]
  $scope.selectedSymbol = $routeParams.symbol getOrElse $cookies.getOrElse("symbol", "AAPL")

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  @scoped
  def expandSectorForSymbol(aSymbol: js.UndefOr[String]) = aSymbol foreach expandSymbolsSector

  @scoped
  def expandOrCollapseSector(aSector: js.UndefOr[Sector]) = aSector foreach toggleSector

  @scoped
  def expandOrCollapseIndustry(aSector: js.UndefOr[Sector], aIndustry: js.UndefOr[Industry]) = {
    for {
      sector <- aSector.toOption
      industry <- aIndustry.toOption
    } {
      toggleIndustry(sector, industry)
    }
  }

  @scoped
  def expandOrCollapseSubIndustry(aSector: js.UndefOr[Sector], aIndustry: js.UndefOr[Industry], aSubIndustry: js.UndefOr[SubIndustry]) = {
    for {
      sector <- aSector.toOption
      industry <- aIndustry.toOption
      subIndustry <- aSubIndustry.toOption
    } {
      toggleSubIndustry(sector, industry, subIndustry)
    }
  }

  @scoped
  def refreshTree() {
    exploreService.loadSectors() onComplete {
      case Success(data) =>
        $scope.sectors = data.map { v => Sector(label = v._id, total = v.total) }
        console.log(s"Loaded ${data.length} sectors")

        // expand the sector, industry, sub-industry for the current symbol
        $timeout(() => expandSymbolsSector($scope.selectedSymbol), 500)
      case Failure(e) =>
        toaster.error("Failed to refresh sector information")
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def expandSymbolsSector(symbol: String) = {
    console.log(s"Attempting to expand sectors for symbol $symbol...")
    for {
      info <- exploreService.loadSectorInfo(symbol)

      _ = console.log(s"Expanding sector ${info.sector}...")
      sector <- expandSector(info)

      _ = console.log(s"Expanding industry ${info.industry}...")
      industry <- expandIndustry(info, sector.get) if sector.isDefined

      _ = console.log(s"Expanding subIndustry ${info.subIndustry}...")
      subIndustry <- expandSubIndustry(info, sector.get, industry.get) if industry.isDefined
    } {
      //$location.hash(symbol)
      $anchorScroll(symbol)
    }
  }

  private def expandSector(info: SectorInfo): Future[Option[Sector]] = {
    val result = for {
      sectorName <- info.sector.toOption
      sector <- findLabel($scope.sectors, sectorName)
      expanded = sector.expanded.exists(_ == true)
    } yield (sector, expanded)

    result match {
      case Some((sector, expanded)) if !expanded =>
        sector.loading = true
        exploreService.loadIndustries(sector.label) map { data =>
          sector.loading = false
          sector.industries = data.map { v => Industry(label = v._id, total = v.total) }
          sector.expanded = true
          Some(sector)
        }
      case _ => Future.successful(None)
    }
  }

  private def expandIndustry(info: SectorInfo, sector: Sector): Future[Option[Industry]] = {
    val result = for {
      industryName <- info.industry.toOption
      industry <- findLabel(sector.industries, industryName)
      expanded = industry.expanded.exists(_ == true)
    } yield (industry, expanded)

    result match {
      case Some((industry, expanded)) if !expanded =>
        industry.loading = true
        exploreService.loadSubIndustries(sector.label, industry.label) map { data =>
          industry.loading = false
          industry.subIndustries = data.map { v => SubIndustry(label = v._id, total = v.total) }
          industry.expanded = true
          Some(industry)
        }
      case _ => Future.successful(None)
    }
  }

  private def expandSubIndustry(info: SectorInfo, sector: Sector, industry: Industry): Future[Option[SubIndustry]] = {
    val result = for {
      subIndustryName <- info.subIndustry.toOption
      subIndustry <- findLabel(industry.subIndustries, subIndustryName)
      expanded = subIndustry.expanded.exists(_ == true)
    } yield (subIndustry, expanded)

    result match {
      case Some((subIndustry, expanded)) if !expanded =>
        subIndustry.loading = true
        exploreService.loadIndustryQuotes(sector.label, industry.label, subIndustry.label) map { quotes =>
          subIndustry.loading = false
          subIndustry.quotes = quotes
          subIndustry.expanded = true
          Some(subIndustry)
        }
      case _ => Future.successful(None)
    }
  }

  private def toggleSector(sector: Sector) {
    if (!sector.expanded.exists(_ == true)) {
      sector.loading = true
      exploreService.loadIndustries(sector.label) onComplete {
        case Success(data) =>
          sector.loading = false
          sector.industries = data.map { v => Industry(label = v._id, total = v.total) }
          sector.expanded = true
        case Failure(e) => sector.loading = false
      }
    }
    else sector.expanded = false
  }

  private def toggleIndustry(sector: Sector, industry: Industry) {
    if (!industry.expanded.exists(_ == true)) {
      industry.loading = true
      exploreService.loadSubIndustries(sector.label, industry.label) onComplete {
        case Success(data) =>
          industry.loading = false
          industry.subIndustries = data.map { v => SubIndustry(label = v._id, total = v.total) }
          industry.expanded = true
        case Failure(e) => industry.loading = false
      }
    }
    else industry.expanded = false
  }

  private def toggleSubIndustry(sector: Sector, industry: Industry, subIndustry: SubIndustry) {
    if (!subIndustry.expanded.exists(_ == true)) {
      subIndustry.loading = true
      val mySubIndustry = if (isDefined(subIndustry)) subIndustry.label else null
      exploreService.loadIndustryQuotes(sector.label, industry.label, mySubIndustry) onComplete {
        case Success(quotes) =>
          subIndustry.loading = false
          subIndustry.quotes = quotes
          subIndustry.expanded = true
        case Failure(e) => subIndustry.loading = false
      }
    }
    else subIndustry.expanded = false
  }

  private def findLabel[T <: QuoteContainer](array: js.Array[T], label: String): Option[T] = array.find(_.label == label)

}


/**
 * Explore Controller
 * @author lawrence.daniels@gmail.com
 */
object ExploreController {

  /**
   * Explore Scope
   * @author lawrence.daniels@gmail.com
   */
  trait ExploreScope extends Scope {
    var sectors: js.Array[Sector] = js.native
    var selectedSymbol: String = js.native
  }

  /**
   * Explore Route Params
   * @author lawrence.daniels@gmail.com
   */
  trait ExploreRouteParams extends js.Object {
    var symbol: js.UndefOr[String] = js.native
  }

  /**
   * An abstract entity that represents a Sector, Industry or Sub-Industry
   */
  trait QuoteContainer extends js.Object {
    var label: String = js.native
    var total: Int = js.native
    var expanded: js.UndefOr[Boolean] = js.native
    var loading: Boolean = js.native
  }

  /**
   * Sector Definition
   */
  trait Sector extends QuoteContainer {
    var industries: js.Array[Industry] = js.native
  }

  /**
   * Sector Singleton
   */
  object Sector {
    def apply(label: String, total: Int) = {
      val sector = makeNew[Sector]
      sector.label = label
      sector.total = total
      sector
    }
  }

  /**
   * Industry Definition
   */
  trait Industry extends QuoteContainer {
    var subIndustries: js.Array[SubIndustry] = js.native
  }

  /**
   * Industry Singleton
   */
  object Industry {
    def apply(label: String, total: Int) = {
      val industry = makeNew[Industry]
      industry.label = label
      industry.total = total
      industry
    }
  }

  /**
   * Sub-industry Definition
   */
  trait SubIndustry extends QuoteContainer {
    var quotes: js.Array[SectorQuote] = js.native
  }

  /**
   * Sub-industry Singleton
   */
  object SubIndustry {
    def apply(label: String, total: Int) = {
      val subIndustry = makeNew[SubIndustry]
      subIndustry.label = label
      subIndustry.total = total
      subIndustry
    }
  }

}
