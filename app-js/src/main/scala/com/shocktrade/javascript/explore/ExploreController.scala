package com.shocktrade.javascript.explore

import com.github.ldaniels528.scalascript.core.{Location, Timeout}
import com.github.ldaniels528.scalascript.extensions.{AnchorScroll, Cookies, Toaster}
import com.github.ldaniels528.scalascript.{Controller, Scope, injected, scoped}
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.explore.ExploreController._
import com.shocktrade.javascript.explore.ExploreService.SectorQuote
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Explore Controller
 * @author lawrence.daniels@gmail.com
 */
class ExploreController($scope: ExploreScope, $anchorScroll: AnchorScroll, $cookies: Cookies,
                        $location: Location, $routeParams: ExploreRouteParams, $timeout: Timeout, toaster: Toaster,
                        @injected("ExploreService") exploreService: ExploreService)
  extends Controller {

  // define the callback signatures
  type SectorCallBackType = js.Function2[Sector, js.Array[Industry], Unit]
  type IndustryCallBackType = js.Function3[Sector, Industry, js.Array[SubIndustry], Unit]
  type SubIndustryCallBackType = js.Function4[Sector, Industry, SubIndustry, js.Array[SectorQuote], Unit]

  // initialize scope variables
  $scope.sectors = emptyArray[Sector]
  $scope.selectedSymbol = $routeParams.symbol getOrElse $cookies.getOrElse("symbol", "AAPL")

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  @scoped
  def expandOrCollapseSector(aSector: js.UndefOr[Sector], callback: js.UndefOr[SectorCallBackType]) = {
    aSector.toOption.foreach(_expandOrCollapseSector(_, callback))
  }

  @scoped
  def expandOrCollapseIndustry(aSector: js.UndefOr[Sector],
                               aIndustry: js.UndefOr[Industry],
                               callback: js.UndefOr[IndustryCallBackType]) = {
    for {
      sector <- aSector.toOption
      industry <- aIndustry.toOption
    } {
      _expandOrCollapseIndustry(sector, industry, callback)
    }
  }

  @scoped
  def expandOrCollapseSubIndustry(aSector: js.UndefOr[Sector],
                                  aIndustry: js.UndefOr[Industry],
                                  aSubIndustry: js.UndefOr[SubIndustry],
                                  callback: js.UndefOr[SubIndustryCallBackType]) = {
    for {
      sector <- aSector.toOption
      industry <- aIndustry.toOption
      subIndustry <- aSubIndustry.toOption
    } {
      _expandOrCollapseSubIndustry(sector, industry, subIndustry, callback)
    }
  }

  @scoped
  def expandSectorForSymbol(aSymbol: js.UndefOr[String]) = aSymbol foreach _expandSectorForSymbol

  @scoped
  def refreshTree() {
    exploreService.loadSectors() onComplete {
      case Success(data) =>
        $scope.sectors = data.map { v => Sector(label = v._id, total = v.total) }
        console.log(s"Loaded ${data.length} sectors")

        // expand the sector, industry, sub-industry for the current symbol
        $timeout(() => _expandSectorForSymbol($scope.selectedSymbol), 1000)
      case Failure(e) =>
        toaster.error("Failed to refresh sector information")
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def _expandSectorForSymbol(symbol: String) {
    // lookup the symbol"s sector information
    exploreService.loadSectorInfo(symbol) onComplete {
      case Success(info) =>
        console.log(s"Attempting to expand symbol $symbol - ${JSON.stringify(info)}")

        // find the symbol (expand: sector >> industry >> sub-industry >> symbol)
        console.log(s"Expanding sector '${info.sector}'...")
        findLabel($scope.sectors, info.sector) foreach { mySector =>
          console.log(s"mySector is '${JSON.stringify(mySector)}'...")
          _expandOrCollapseSector(mySector, { (sector: Sector, industries: js.Array[Industry]) =>

            console.log(s"Expanding industry '${info.sector}' >> '${info.industry}'...")
            findLabel(industries, info.industry) foreach { myIndustry =>
              console.log(s"myIndustry is '${JSON.stringify(myIndustry)}'...")
              _expandOrCollapseIndustry(sector, myIndustry, { (sector: Sector, industry: Industry, subIndustries: js.Array[SubIndustry]) =>

                console.log(s"Expanding sub-industry '${info.sector}' >> '${info.industry}' >> '${info.subIndustry}'...")
                findLabel(subIndustries, info.subIndustry) foreach { mySubIndustry =>
                  _expandOrCollapseSubIndustry(sector, industry, mySubIndustry, { (sector: Sector, industry: Industry, subIndustry: SubIndustry, quotes: js.Array[SectorQuote]) =>
                    $location.hash("10000")
                    $anchorScroll()
                    ()
                  }: SubIndustryCallBackType)
                }
              }: IndustryCallBackType)
            }
          }: SectorCallBackType)
        }
      case Failure(e) =>
        toaster.error("Error loading sector information")
    }
  }

  private def _expandOrCollapseSector(sector: Sector, callback: js.UndefOr[SectorCallBackType]) {
    if (!sector.expanded.exists(_ == true)) {
      sector.loading = true
      exploreService.loadIndustries(sector.label) onComplete {
        case Success(data) =>
          sector.loading = false
          sector.industries = data.map { v => Industry(label = v._id, total = v.total) }
          sector.expanded = true
          callback.foreach(_(sector, sector.industries))
        case Failure(e) => sector.loading = false
      }
    }
    else sector.expanded = false
  }

  private def _expandOrCollapseIndustry(sector: Sector, industry: Industry, callback: js.UndefOr[IndustryCallBackType]) {
    if (!industry.expanded.exists(_ == true)) {
      industry.loading = true
      exploreService.loadSubIndustries(sector.label, industry.label) onComplete {
        case Success(data) =>
          industry.loading = false
          industry.subIndustries = data.map { v => SubIndustry(label = v._id, total = v.total) }
          industry.expanded = true
          callback.foreach(_(sector, industry, industry.subIndustries))
        case Failure(e) => industry.loading = false
      }
    }
    else industry.expanded = false
  }

  private def _expandOrCollapseSubIndustry(sector: Sector,
                                           industry: Industry,
                                           subIndustry: SubIndustry,
                                           callback: js.UndefOr[SubIndustryCallBackType]) {
    if (!subIndustry.expanded.exists(_ == true)) {
      subIndustry.loading = true
      val mySubIndustry = if (isDefined(subIndustry)) subIndustry.label else null
      exploreService.loadIndustryQuotes(sector.label, industry.label, mySubIndustry) onComplete {
        case Success(quotes) =>
          subIndustry.loading = false
          subIndustry.quotes = quotes
          subIndustry.expanded = true
          callback.foreach(_(sector, industry, subIndustry, subIndustry.quotes))
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
