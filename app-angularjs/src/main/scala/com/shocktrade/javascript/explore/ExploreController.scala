package com.shocktrade.javascript.explore

import com.shocktrade.javascript.models.quote.SectorInfoQuote
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs.anchorscroll.AnchorScroll
import org.scalajs.angularjs.cookies.Cookies
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Controller, Location, Scope, Timeout, angular, injected}
import org.scalajs.dom.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
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
  $scope.sectors = emptyArray
  $scope.selectedSymbol = $routeParams.symbol ?? $cookies.getOrElse("symbol", "AAPL")

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.expandSectorForSymbol = (aSymbol: js.UndefOr[String]) => aSymbol foreach expandAllForSymbol

  $scope.expandOrCollapseIndustry = (aSector: js.UndefOr[Sector], aIndustry: js.UndefOr[Industry]) => {
    for {
      sector <- aSector
      industry <- aIndustry
    } toggleIndustry(sector, industry)
  }

  $scope.expandOrCollapseSector = (aSector: js.UndefOr[Sector]) => aSector foreach toggleSector

  $scope.expandOrCollapseSubIndustry = (aSector: js.UndefOr[Sector], aIndustry: js.UndefOr[Industry], aSubIndustry: js.UndefOr[SubIndustry]) => {
    for {
      sector <- aSector
      industry <- aIndustry
      subIndustry <- aSubIndustry
    } toggleSubIndustry(sector, industry, subIndustry)
  }

  $scope.refreshTree = () => {
    exploreService.loadSectors() onComplete {
      case Success(data) =>
        console.log(s"Loaded ${data.length} sectors")
        $scope.$apply(() => $scope.sectors = data.map(v => new Sector(label = v._id, total = v.total)))
        $scope.selectedSymbol foreach { symbol => $timeout(() => expandAllForSymbol(symbol), 500) }
      case Failure(e) =>
        toaster.error("Failed to refresh sector information")
        console.error(e.displayMessage)
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def expandAllForSymbol(symbol: String) {
    console.log(s"Attempting to expand sectors for symbol $symbol...")
    val startTime = System.currentTimeMillis()
    val results = for {
      info <- exploreService.loadSectorInfo(symbol)
      _ = console.log(s"info => ${angular.toJson(info)}")

      _ = console.log(s"Expanding sector ${info.sector}...")
      sectorOpt <- expandSector(info)

      _ = console.log(s"Expanding industry ${info.industry}...")
      industryOpt <- sectorOpt match {
        case Some(sector) => expandIndustry(info, sector)
        case None => Future.successful(None)
      }

      _ = console.log(s"Expanding sub-industry ${info.subIndustry}...")
      subIndustryOpt <- (sectorOpt, industryOpt) match {
        case (Some(sector), Some(industry)) => expandSubIndustry(info, sector, industry)
        case _ => Future.successful(None)
      }

    } yield (info, sectorOpt, industryOpt, subIndustryOpt)

    results onComplete {
      case Success((info, sectorOpt, industryOpt, subIndustryOpt)) =>
        console.log(s"Finished expanding sectors in ${System.currentTimeMillis() - startTime} msecs")
        $location.hash(symbol)
        $anchorScroll(symbol)
      case Failure(e) =>
        toaster.error(e.getMessage)
        console.error(e.getMessage)
    }
  }

  private def expandSector(info: SectorInfoQuote): Future[Option[Sector]] = {
    val result = for {
      sectorName <- info.sector.toOption
      sector <- $scope.sectors.find(_.label == sectorName)
    } yield (sector, sector.expanded.isTrue)

    result match {
      case Some((sector, expanded)) if !expanded =>
        console.info(s"Loading industries for ${info.symbol} => ${sector.label}")
        sector.loading = true
        exploreService.loadIndustries(sector.label) map (updateSector(sector, _))
      case _ => Future.successful(None)
    }
  }

  private def expandIndustry(info: SectorInfoQuote, sector: Sector): Future[Option[Industry]] = {
    val result = for {
      industryName <- info.industry.toOption
      industry <- sector.industries.find(_.label == industryName)
    } yield (industry, industry.expanded.isTrue)

    result match {
      case Some((industry, expanded)) if !expanded =>
        industry.loading = true
        exploreService.loadSubIndustries(sector.label, industry.label) map (updateIndustry(industry, _))
      case _ => Future.successful(None)
    }
  }

  private def expandSubIndustry(info: SectorInfoQuote, sector: Sector, industry: Industry): Future[Option[SubIndustry]] = {
    val result = for {
      subIndustryName <- info.subIndustry.toOption
      subIndustry <- industry.subIndustries.find(_.label == subIndustryName)
    } yield (subIndustry, subIndustry.expanded.isTrue)

    result match {
      case Some((subIndustry, expanded)) if !expanded =>
        subIndustry.loading = true
        exploreService.loadIndustryQuotes(sector.label, industry.label, subIndustry.label) map (updateSubIndustry(subIndustry, _))
      case _ => Future.successful(None)
    }
  }

  private def toggleSector(sector: Sector) {
    if (!sector.expanded.isTrue) {
      sector.loading = true
      exploreService.loadIndustries(sector.label) onComplete {
        case Success(data) => updateSector(sector, data)
        case Failure(e) => sector.loading = false
      }
    }
    else sector.expanded = false
  }

  private def toggleIndustry(sector: Sector, industry: Industry) {
    if (!industry.expanded.isTrue) {
      industry.loading = true
      exploreService.loadSubIndustries(sector.label, industry.label) onComplete {
        case Success(data) => updateIndustry(industry, data)
        case Failure(e) => industry.loading = false
      }
    }
    else industry.expanded = false
  }

  private def toggleSubIndustry(sector: Sector, industry: Industry, subIndustry: SubIndustry) {
    if (!subIndustry.expanded.isTrue) {
      subIndustry.loading = true
      exploreService.loadIndustryQuotes(sector.label, industry.label, subIndustry.label) onComplete {
        case Success(quotes) => updateSubIndustry(subIndustry, quotes)
        case Failure(e) => subIndustry.loading = false
      }
    }
    else subIndustry.expanded = false
  }

  private def updateIndustry(industry: Industry, data: js.Array[AggregatedSectorData]) = {
    $scope.$apply(() => {
      industry.loading = false
      industry.subIndustries = data.map { v => new SubIndustry(label = v._id, total = v.total) }
      industry.expanded = true
    })
    Some(industry)
  }

  private def updateSector(sector: Sector, data: js.Array[AggregatedSectorData]) = {
    $scope.$apply(() => {
      sector.loading = false
      sector.industries = data.map(v => new Industry(label = v._id, total = v.total))
      sector.expanded = true
    })
    Some(sector)
  }

  private def updateSubIndustry(subIndustry: SubIndustry, quotes: js.Array[SectorQuote]) = {
    $scope.$apply(() => {
      subIndustry.loading = false
      subIndustry.quotes = quotes
      subIndustry.expanded = true
    })
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
  var sectors: js.Array[Sector] = js.native
  var selectedSymbol: js.UndefOr[String] = js.native

  // functions
  var expandSectorForSymbol: js.Function1[js.UndefOr[String], Unit] = js.native
  var expandOrCollapseSector: js.Function1[js.UndefOr[Sector], Unit] = js.native
  var expandOrCollapseIndustry: js.Function2[js.UndefOr[Sector], js.UndefOr[Industry], Unit] = js.native
  var expandOrCollapseSubIndustry: js.Function3[js.UndefOr[Sector], js.UndefOr[Industry], js.UndefOr[SubIndustry], Unit] = js.native
  var refreshTree: js.Function0[Unit] = js.native
}

/**
  * Explore Route Params
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ExploreRouteParams extends js.Object {
  var symbol: js.UndefOr[String] = js.native
}

/**
  * Sector Definition
  */
@ScalaJSDefined
class Sector(val label: String, val total: Int) extends js.Object {
  var industries: js.Array[Industry] = emptyArray
  var expanded: js.UndefOr[Boolean] = js.undefined
  var loading: js.UndefOr[Boolean] = js.undefined
}

/**
  * Industry Definition
  */
@ScalaJSDefined
class Industry(val label: String, val total: Int) extends js.Object {
  var subIndustries: js.Array[SubIndustry] = emptyArray
  var expanded: js.UndefOr[Boolean] = js.undefined
  var loading: js.UndefOr[Boolean] = js.undefined
}

/**
  * Sub-industry Definition
  */
@ScalaJSDefined
class SubIndustry(val label: String, val total: Int) extends js.Object {
  var quotes: js.Array[SectorQuote] = emptyArray
  var expanded: js.UndefOr[Boolean] = js.undefined
  var loading: js.UndefOr[Boolean] = js.undefined
}

