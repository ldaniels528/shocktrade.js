package com.shocktrade.client.discover

import com.shocktrade.client.discover.ExploreController._
import com.shocktrade.client.discover.ExploreService.AggregatedSectorData
import com.shocktrade.client.{GlobalSelectedSymbol, GlobalSelectedSymbolScope}
import com.shocktrade.common.models.quote.{ResearchQuote, SectorInfoQuote}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.anchorscroll.AnchorScroll
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Location, Timeout, injected}
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Explore Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class ExploreController($scope: ExploreControllerScope, $anchorScroll: AnchorScroll, $cookies: Cookies, $location: Location,
                             $routeParams: ExploreRouteParams, $timeout: Timeout, toaster: Toaster,
                             @injected("ExploreService") exploreService: ExploreService)
  extends Controller with GlobalSelectedSymbol {

  // initialize scope variables
  $scope.sectors = emptyArray
  //$scope.selectedSymbol = $routeParams.symbol ?? $cookies.getOrElse("symbol", "AAPL")

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.expandSectorForSymbol = (aSymbol: js.UndefOr[String]) => aSymbol foreach expandAllForSymbol

  $scope.collapseOrExpandSector = (aSector: js.UndefOr[Sector]) => aSector foreach { sector =>
    if (!sector.expanded.isTrue) {
      sector.loading = true
      exploreService.loadIndustries(sector.label) onComplete {
        case Success(response) =>
          $scope.$apply(() => updateSector(sector, response.data))
        case Failure(e) =>
          $scope.$apply(() => sector.loading = false)
          toaster.error(s"Error expanding ${sector.label}")
          console.error(s"Error expanding ${sector.label}: ${e.displayMessage}")
      }
    }
    else sector.expanded = false
  }

  $scope.collapseOrExpandIndustry = (aSector: js.UndefOr[Sector], aIndustry: js.UndefOr[Industry]) => {
    for {
      sector <- aSector
      industry <- aIndustry
    } {
      if (!industry.expanded.isTrue) {
        industry.loading = true
        val outcome = for {
          subIndustries <- exploreService.loadSubIndustries(sector.label, industry.label).map(_.data)
          quotes <- exploreService.loadIndustryQuotes(sector.label, industry.label).map(_.data)
        } yield (subIndustries, quotes)

        outcome onComplete {
          case Success((subIndustries, quotes)) =>
            $scope.$apply(() => updateIndustry(industry, subIndustries, quotes))
          case Failure(e) =>
            $scope.$apply(() => industry.loading = false)
            toaster.error(s"Error expanding ${industry.label}")
            console.error(s"Error expanding ${industry.label}: ${e.displayMessage}")
        }
      }
      else industry.expanded = false
    }
  }

  $scope.collapseOrExpandSubIndustry = (aSector: js.UndefOr[Sector], aIndustry: js.UndefOr[Industry], aSubIndustry: js.UndefOr[SubIndustry]) => {
    for {
      sector <- aSector
      industry <- aIndustry
      subIndustry <- aSubIndustry
    } {
      if (!subIndustry.expanded.isTrue) {
        subIndustry.loading = true
        exploreService.loadSubIndustryQuotes(sector.label, industry.label, subIndustry.label) onComplete {
          case Success(response) =>
            $scope.$apply(() => updateSubIndustry(subIndustry, response.data))
          case Failure(e) =>
            $scope.$apply(() => subIndustry.loading = false)
            toaster.error(s"Error expanding ${subIndustry.label}")
            console.error(s"Error expanding ${subIndustry.label}: ${e.displayMessage}")
        }
      }
      else subIndustry.expanded = false
    }
  }

  $scope.refreshTree = () => {
    exploreService.loadSectors() onComplete {
      case Success(response) =>
        val data = response.data
        console.log(s"Loaded ${data.length} sectors")
        $scope.$apply(() => $scope.sectors = data.map(v => new Sector(label = v._id, total = v.total)))
        $scope.selectedSymbol foreach { symbol => $timeout(() => expandAllForSymbol(symbol), 500) }
      case Failure(e) =>
        toaster.error("Failed to refresh sector information")
        console.error(s"Failed to refresh sector information: ${e.displayMessage}")
    }
  }

  $scope.selectQuote = (aQuote: js.UndefOr[ResearchQuote]) => aQuote foreach { quote =>
    $scope.selectedSymbol = quote.symbol
  }

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  override def onSymbolSelected(newSymbol: String, oldSymbol: Option[String]) {
    if (!oldSymbol.contains(newSymbol)) {
      exploreService.loadSectorInfo(newSymbol).map(_.data) onComplete {
        case Success(info) =>
          $location.search("symbol", newSymbol)
          $scope.$apply(() => $scope.q = info)
        case Failure(e) =>
          console.error(s"Failed to load sector info for $newSymbol")
      }
    }
  }

  private def expandAllForSymbol(symbol: String) {
    console.log(s"Attempting to expand sectors for symbol $symbol...")
    val startTime = System.currentTimeMillis()
    val results = for {
      info <- exploreService.loadSectorInfo(symbol).map(_.data)
      sectorOpt <- expandSector(info)
      industryOpt <- sectorOpt.map(expandIndustry(info, _)) getOrElse Future.successful(None)
      subIndustryOpt <- (sectorOpt, industryOpt) match {
        case (Some(sector), Some(industry)) => expandSubIndustry(info, sector, industry)
        case _ => Future.successful(None)
      }
    } yield (info, sectorOpt, industryOpt, subIndustryOpt)

    results onComplete {
      case Success((info, sectorOpt, industryOpt, subIndustryOpt)) =>
        console.log(s"Finished expanding sectors in ${System.currentTimeMillis() - startTime} msecs")
        $scope.q = info
        $location.search("symbol", symbol)
        $location.hash(symbol)
        $anchorScroll(symbol)
      case Failure(e) =>
        toaster.error(s"Error expanding $symbol")
        console.error(s"Error expanding $symbol: ${e.displayMessage}")
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
        exploreService.loadIndustries(sector.label).map(_.data) map (updateSector(sector, _))
      case _ => Future.successful(None)
    }
  }

  private def updateSector(sector: Sector, data: js.Array[AggregatedSectorData]) = {
    sector.loading = false
    sector.industries = data.map(v => new Industry(label = v._id, total = v.total))
    sector.expanded = true
    Some(sector)
  }

  private def expandIndustry(info: SectorInfoQuote, sector: Sector): Future[Option[Industry]] = {
    val result = for {
      industryName <- info.industry.toOption
      industry <- sector.industries.toOption.flatMap(_.find(_.label == industryName))
    } yield (industry, industry.expanded.isTrue)

    result match {
      case Some((industry, expanded)) if !expanded =>
        industry.loading = true
        val outcome = for {
          subIndustries <- exploreService.loadSubIndustries(sector.label, industry.label).map(_.data)
          quotes <- exploreService.loadIndustryQuotes(sector.label, industry.label).map(_.data)
        } yield (subIndustries, quotes)

        outcome map { case (subIndustries, quotes) => updateIndustry(industry, subIndustries, quotes) }
      case _ => Future.successful(None)
    }
  }

  private def updateIndustry(industry: Industry, data: js.Array[AggregatedSectorData], quotes: js.Array[ResearchQuote]) = {
    industry.loading = false
    industry.quotes = quotes
    industry.subIndustries = data.map { v => new SubIndustry(label = v._id, total = v.total) }
    industry.expanded = true
    Some(industry)
  }

  private def expandSubIndustry(info: SectorInfoQuote, sector: Sector, industry: Industry): Future[Option[SubIndustry]] = {
    val result = for {
      subIndustryName <- info.subIndustry.toOption
      subIndustry <- industry.subIndustries.toOption.flatMap(_.find(_.label == subIndustryName))
    } yield (subIndustry, subIndustry.expanded.isTrue)

    result match {
      case Some((subIndustry, expanded)) if !expanded =>
        subIndustry.loading = true
        exploreService.loadSubIndustryQuotes(sector.label, industry.label, subIndustry.label).map(_.data) map (updateSubIndustry(subIndustry, _))
      case _ => Future.successful(None)
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Event Listeners
  /////////////////////////////////////////////////////////////////////

  private def updateSubIndustry(subIndustry: SubIndustry, quotes: js.Array[ResearchQuote]) = {
    subIndustry.loading = false
    subIndustry.quotes = quotes
    subIndustry.expanded = true
    Some(subIndustry)
  }

}

/**
 * Explore Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ExploreController {

  /**
   * Explore Route Params
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait ExploreRouteParams extends js.Object {
    var symbol: js.UndefOr[String] = js.native
  }

  /**
   * Industry-Like Definition
   */
  trait IndustryLike extends js.Object {
    val label: String
    val total: Int
    var quotes: js.UndefOr[js.Array[ResearchQuote]]
    var expanded: js.UndefOr[Boolean]
    var loading: js.UndefOr[Boolean]
  }

  /**
   * Sector Definition
   */
  class Sector(val label: String, val total: Int) extends js.Object {
    var industries: js.UndefOr[js.Array[Industry]] = js.undefined
    var expanded: js.UndefOr[Boolean] = js.undefined
    var loading: js.UndefOr[Boolean] = js.undefined
  }

  /**
   * Industry Definition
   */
  class Industry(val label: String, val total: Int) extends IndustryLike {
    override var quotes: js.UndefOr[js.Array[ResearchQuote]] = js.undefined
    override var expanded: js.UndefOr[Boolean] = js.undefined
    override var loading: js.UndefOr[Boolean] = js.undefined
    var subIndustries: js.UndefOr[js.Array[SubIndustry]] = js.undefined
  }

  /**
   * Sub-industry Definition
   */
  class SubIndustry(val label: String, val total: Int) extends IndustryLike {
    override var quotes: js.UndefOr[js.Array[ResearchQuote]] = js.undefined
    override var expanded: js.UndefOr[Boolean] = js.undefined
    override var loading: js.UndefOr[Boolean] = js.undefined
  }

}

/**
 * Explore Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait ExploreControllerScope extends GlobalSelectedSymbolScope {
  // variables
  var q: js.UndefOr[SectorInfoQuote] = js.native
  var sectors: js.Array[Sector] = js.native

  // functions
  var collapseOrExpandSector: js.Function1[js.UndefOr[Sector], Unit] = js.native
  var collapseOrExpandIndustry: js.Function2[js.UndefOr[Sector], js.UndefOr[Industry], Unit] = js.native
  var collapseOrExpandSubIndustry: js.Function3[js.UndefOr[Sector], js.UndefOr[Industry], js.UndefOr[SubIndustry], Unit] = js.native
  var expandSectorForSymbol: js.Function1[js.UndefOr[String], Unit] = js.native
  var refreshTree: js.Function0[Unit] = js.native
  var selectQuote: js.Function1[js.UndefOr[ResearchQuote], Unit] = js.native

}


