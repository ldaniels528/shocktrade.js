package com.shocktrade.javascript.explore

import com.github.ldaniels528.meansjs.angularjs.{Location, Timeout}
import com.github.ldaniels528.meansjs.angularjs.AnchorScroll
import com.github.ldaniels528.meansjs.angularjs.cookies.Cookies
import com.github.ldaniels528.meansjs.angularjs.toaster.Toaster
import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.github.ldaniels528.meansjs.angularjs.{Controller, Scope, injected}
import org.scalajs.dom.console

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
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
  $scope.selectedSymbol = if ($routeParams.symbol.nonEmpty) $routeParams.symbol else $cookies.getOrElse("symbol", "AAPL")

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.expandSectorForSymbol = (aSymbol: js.UndefOr[String]) => aSymbol foreach expandSymbolsSector

  $scope.expandOrCollapseSector = (aSector: js.UndefOr[Sector]) => aSector foreach toggleSector

  $scope.expandOrCollapseIndustry = (aSector: js.UndefOr[Sector], aIndustry: js.UndefOr[Industry]) => {
    for {sector <- aSector.toOption; industry <- aIndustry.toOption} toggleIndustry(sector, industry)
  }

  $scope.expandOrCollapseSubIndustry = (aSector: js.UndefOr[Sector], aIndustry: js.UndefOr[Industry], aSubIndustry: js.UndefOr[SubIndustry]) => {
    for {
      sector <- aSector.toOption
      industry <- aIndustry.toOption
      subIndustry <- aSubIndustry.toOption
    } toggleSubIndustry(sector, industry, subIndustry)
  }

  $scope.refreshTree = () => {
    exploreService.loadSectors() onComplete {
      case Success(data) =>
        $scope.sectors = data.map { v => Sector(label = v._id, total = v.total) }
        console.log(s"Loaded ${data.length} sectors")
        $scope.selectedSymbol foreach { symbol => $timeout(() => expandSymbolsSector(symbol), 500) }
      case Failure(e) =>
        toaster.error("Failed to refresh sector information")
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def expandSymbolsSector(symbol: String) {
    console.log(s"Attempting to expand sectors for symbol $symbol...")
    val startTime = System.currentTimeMillis()
    val results = for {
      info <- exploreService.loadSectorInfo(symbol)

      _ = console.log(s"Expanding sector ${info.sector}...")
      sector <- expandSector(info)

      _ = console.log(s"Expanding industry ${info.industry}...")
      industry <- expandIndustry(info, sector.get) if sector.isDefined

      _ = console.log(s"Expanding sub-industry ${info.subIndustry}...")
      subIndustry <- expandSubIndustry(info, sector.get, industry.get) if industry.isDefined
    } yield (info, sector, industry, subIndustry)

    results onComplete {
      case Success((info, sector, industry, subIndustry)) =>
        console.log(s"Finished expanding sectors in ${System.currentTimeMillis() - startTime} msecs")
        $location.hash(symbol)
        $anchorScroll(symbol)
      case Failure(e) =>
        toaster.error(e.getMessage)
        console.error(e.getMessage)
    }
  }

  private def expandSector(info: SectorInfo): Future[Option[Sector]] = {
    val result = for {
      sectorName <- info.sector.toOption
      sector <- $scope.sectors.find(_.label == sectorName)
    } yield (sector, sector.expanded.contains(true))

    result match {
      case Some((sector, expanded)) if !expanded =>
        sector.loading = true
        exploreService.loadIndustries(sector.label) map (updateSector(sector, _))
      case _ => Future.successful(None)
    }
  }

  private def expandIndustry(info: SectorInfo, sector: Sector): Future[Option[Industry]] = {
    val result = for {
      industryName <- info.industry.toOption
      industry <- sector.industries.find(_.label == industryName)
    } yield (industry, industry.expanded.contains(true))

    result match {
      case Some((industry, expanded)) if !expanded =>
        industry.loading = true
        exploreService.loadSubIndustries(sector.label, industry.label) map (updateIndustry(industry, _))
      case _ => Future.successful(None)
    }
  }

  private def expandSubIndustry(info: SectorInfo, sector: Sector, industry: Industry): Future[Option[SubIndustry]] = {
    val result = for {
      subIndustryName <- info.subIndustry.toOption
      subIndustry <- industry.subIndustries.find(_.label == subIndustryName)
    } yield (subIndustry, subIndustry.expanded.contains(true))

    result match {
      case Some((subIndustry, expanded)) if !expanded =>
        subIndustry.loading = true
        exploreService.loadIndustryQuotes(sector.label, industry.label, subIndustry.label) map (updateSubIndustry(subIndustry, _))
      case _ => Future.successful(None)
    }
  }

  private def toggleSector(sector: Sector) {
    if (!sector.expanded.contains(true)) {
      sector.loading = true
      exploreService.loadIndustries(sector.label) onComplete {
        case Success(data) => updateSector(sector, data)
        case Failure(e) => sector.loading = false
      }
    }
    else sector.expanded = false
  }

  private def toggleIndustry(sector: Sector, industry: Industry) {
    if (!industry.expanded.contains(true)) {
      industry.loading = true
      exploreService.loadSubIndustries(sector.label, industry.label) onComplete {
        case Success(data) => updateIndustry(industry, data)
        case Failure(e) => industry.loading = false
      }
    }
    else industry.expanded = false
  }

  private def toggleSubIndustry(sector: Sector, industry: Industry, subIndustry: SubIndustry) {
    if (!subIndustry.expanded.contains(true)) {
      subIndustry.loading = true
      exploreService.loadIndustryQuotes(sector.label, industry.label, subIndustry.label) onComplete {
        case Success(quotes) => updateSubIndustry(subIndustry, quotes)
        case Failure(e) => subIndustry.loading = false
      }
    }
    else subIndustry.expanded = false
  }

  private def updateSector(sector: Sector, data: js.Array[AggregatedData]) = {
    sector.loading = false
    sector.industries = data.map { v => Industry(label = v._id, total = v.total) }
    sector.expanded = true
    Some(sector)
  }

  private def updateIndustry(industry: Industry, data: js.Array[AggregatedData]) = {
    industry.loading = false
    industry.subIndustries = data.map { v => SubIndustry(label = v._id, total = v.total) }
    industry.expanded = true
    Some(industry)
  }

  private def updateSubIndustry(subIndustry: SubIndustry, quotes: js.Array[SectorQuote]) = {
    subIndustry.loading = false
    subIndustry.quotes = quotes
    subIndustry.expanded = true
    Some(subIndustry)
  }
}

/**
  * Explore Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ExploreScope extends Scope {
  // variables
  var sectors: js.Array[Sector]
  var selectedSymbol: js.UndefOr[String]

  // functions
  var expandSectorForSymbol: js.Function1[js.UndefOr[String], Unit]
  var expandOrCollapseSector: js.Function1[js.UndefOr[Sector], Unit]
  var expandOrCollapseIndustry: js.Function2[js.UndefOr[Sector], js.UndefOr[Industry], Unit]
  var expandOrCollapseSubIndustry: js.Function3[js.UndefOr[Sector], js.UndefOr[Industry], js.UndefOr[SubIndustry], Unit]
  var refreshTree: js.Function0[Unit]
}

/**
  * Explore Route Params
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ExploreRouteParams extends js.Object {
  var symbol: js.UndefOr[String]
}

/**
  * An abstract entity that represents a Sector, Industry or Sub-Industry
  */
@js.native
trait QuoteContainer extends js.Object {
  var label: String
  var total: Int
  var expanded: js.UndefOr[Boolean]
  var loading: js.UndefOr[Boolean]
}

/**
  * Sector Definition
  */
@js.native
trait Sector extends QuoteContainer {
  var industries: js.Array[Industry]
}

/**
  * Sector Singleton
  */
object Sector {
  def apply(label: String, total: Int) = {
    val sector = New[Sector]
    sector.label = label
    sector.total = total
    sector
  }
}

/**
  * Industry Definition
  */
@js.native
trait Industry extends QuoteContainer {
  var subIndustries: js.Array[SubIndustry]
}

/**
  * Industry Singleton
  */
object Industry {
  def apply(label: String, total: Int) = {
    val industry = New[Industry]
    industry.label = label
    industry.total = total
    industry
  }
}

/**
  * Sub-industry Definition
  */
@js.native
trait SubIndustry extends QuoteContainer {
  var quotes: js.Array[SectorQuote]
}

/**
  * Sub-industry Singleton
  */
object SubIndustry {
  def apply(label: String, total: Int) = {
    val subIndustry = New[SubIndustry]
    subIndustry.label = label
    subIndustry.total = total
    subIndustry
  }
}
