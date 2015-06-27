package com.shocktrade.javascript.discover

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.core.{Controller, Location, Timeout}
import com.ldaniels528.javascript.angularjs.extensions.{Cookies, Toaster}
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Explore: Drill-Down Controller
 * @author lawrence.daniels@gmail.com
 */
class DrillDownController($scope: js.Dynamic, $anchorScroll: js.Dynamic, $cookieStore: Cookies,
                          $location: Location, $routeParams: js.Dynamic, $timeout: Timeout, toaster: Toaster,
                          @named("QuoteService") quoteService: QuoteService)
  extends Controller {

  // define the callback signatures
  type SectorCallBackType = js.Function2[js.Dynamic, js.Array[js.Dynamic], Unit]
  type IndustryCallBackType = js.Function3[js.Dynamic, js.Dynamic, js.Array[js.Dynamic], Unit]
  type SubIndustryCallBackType = js.Function4[js.Dynamic, js.Dynamic, js.Dynamic, js.Array[js.Dynamic], Unit]

  // tree data
  private var sectors = emptyArray[js.Dynamic]

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.expandOrCollapseIndustry = (sector: js.Dynamic, industry: js.Dynamic, callback: IndustryCallBackType) =>
    expandOrCollapseIndustry(sector, industry, callback)

  $scope.expandOrCollapseSector = (sector: js.Dynamic, callback: SectorCallBackType) =>
    expandOrCollapseSector(sector, callback)

  $scope.expandOrCollapseSubIndustry = (sector: js.Dynamic, industry: js.Dynamic, subIndustry: js.Dynamic, callback: SubIndustryCallBackType) =>
    expandOrCollapseSubIndustry(sector, industry, subIndustry, callback)

  $scope.expandSectorForSymbol = (symbol: String) => expandSectorForSymbol(symbol)

  $scope.getSectors = () => sectors

  $scope.refreshTree = () => refreshTree()

  $scope.selectedSymbol = () => selectedSymbol

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def expandOrCollapseSector(sector: js.Dynamic, callback: SectorCallBackType) {
    if (!isDefined(sector.expanded) && !sector.expanded.isTrue) {
      sector.loading = true
      quoteService.loadIndustries(sector.label.as[String]) onComplete {
        case Success(data) =>
          sector.loading = false
          sector.industries = data.map { v => JS(label = v._id, total = v.total) }
          sector.expanded = true
          if (isDefined(callback)) callback(sector, sector.industries.asArray[js.Dynamic])
        case Failure(e) => sector.loading = false
      }
    }
    else sector.expanded = false
  }

  private def expandOrCollapseIndustry(sector: js.Dynamic, industry: js.Dynamic, callback: IndustryCallBackType) {
    if (isDefined(industry)) {
      if (!isDefined(industry.expanded) || !industry.expanded.isTrue) {
        industry.loading = true
        quoteService.loadSubIndustries(sector.label.as[String], industry.label.as[String]) onComplete {
          case Success(data) =>
            industry.loading = false
            industry.subIndustries = data.map { v => JS(label = v._id, total = v.total) }
            industry.expanded = true
            if (isDefined(callback)) callback(sector, industry, industry.subIndustries.asArray[js.Dynamic])
          case Failure(e) => industry.loading = false
        }
      }
      else industry.expanded = false
    }
  }

  private def expandOrCollapseSubIndustry(sector: js.Dynamic, industry: js.Dynamic, subIndustry: js.Dynamic, callback: SubIndustryCallBackType) {
    if (isDefined(subIndustry)) {
      if (!isDefined(subIndustry.expanded) || !subIndustry.expanded.isTrue) {
        subIndustry.loading = true
        val mySubIndustry = if (isDefined(subIndustry)) subIndustry.label.as[String] else null
        quoteService.loadIndustryQuotes(sector.label.as[String], industry.label.as[String], mySubIndustry) onComplete {
          case Success(data) =>
            subIndustry.loading = false
            subIndustry.quotes = data
            subIndustry.expanded = true
            if (isDefined(callback)) callback(sector, industry, subIndustry, subIndustry.quotes.asArray[js.Dynamic])
          case Failure(e) => subIndustry.loading = false
        }
      }
      else subIndustry.expanded = false
    }
  }

  private def expandSectorForSymbol(symbol: String) {
    // lookup the symbol"s sector information
    quoteService.loadSectorInfo(symbol) onComplete {
      case Success(data) =>
        val info = data.head
        g.console.log(s"info = ${JSON.stringify(info)}")
        // find the symbol (expand: sector >> industry >> sub-industry >> symbol)
        g.console.log(s"Expanding sector '${info.sector}'...")
        findLabel(sectors, info.sector.as[String]) foreach { mySector =>
          g.console.log(s"mySector is '${JSON.stringify(mySector)}'...")
          expandOrCollapseSector(mySector, { (sector: js.Dynamic, industries: js.Array[js.Dynamic]) =>

            g.console.log(s"Expanding industry '${info.sector}' >> '${info.industry}'...")
            findLabel(industries, info.industry.as[String]) foreach { myIndustry =>
              g.console.log(s"myIndustry is '${JSON.stringify(myIndustry)}'...")
              expandOrCollapseIndustry(sector, myIndustry, { (sector: js.Dynamic, industry: js.Dynamic, subIndustries: js.Array[js.Dynamic]) =>

                g.console.log(s"Expanding sub-industry '${info.sector}' >> '${info.industry}' >> '${info.subIndustry}'...")
                findLabel(subIndustries, info.subIndustry.as[String]) foreach { mySubIndustry =>
                  expandOrCollapseSubIndustry(sector, industry, mySubIndustry, { (sector: js.Dynamic, industry: js.Dynamic, subIndustry: js.Dynamic, quotes: js.Array[js.Dynamic]) =>
                    $location.hash("10000")
                    $anchorScroll()
                    ()
                  })
                }
              })
            }
          })
        }
      case Failure(e) =>
        toaster.error("Error loading sector information")
    }
  }

  private def refreshTree() {
    quoteService.loadSectors() onComplete {
      case Success(data) =>
        sectors = data.map { v => JS(label = v._id, total = v.total) }

        // expand the sector, industry, sub-industry for the current symbol
        $timeout(() => expandSectorForSymbol(selectedSymbol), 500)
      case Failure(e) =>
        toaster.error("Failed to refresh tree")
    }
  }

  private def findLabel(array: js.Array[js.Dynamic], label: String) = array.find(_.label === label)

  private def selectedSymbol = {
    if (isDefined($routeParams.symbol)) $routeParams.symbol.as[String] else $cookieStore.getOrElse("symbol", "AAPL")
  }

}
