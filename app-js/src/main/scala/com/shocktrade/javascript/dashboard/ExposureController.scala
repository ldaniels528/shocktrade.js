package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.meansjs.angularjs.Timeout
import com.github.ldaniels528.meansjs.angularjs.http.Http
import com.github.ldaniels528.meansjs.angularjs.toaster.Toaster
import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.github.ldaniels528.meansjs.angularjs.{Controller, Scope, injected}
import com.shocktrade.javascript.MySessionService
import com.shocktrade.javascript.models.BSONObjectID
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Exposure Controller
  * @author lawrence.daniels@gmail.com
  */
class ExposureController($scope: ExposureScope, $http: Http, $timeout: Timeout, toaster: Toaster,
                         @injected("ContestService") contestService: ContestService,
                         @injected("MySessionService") mySession: MySessionService)
  extends Controller {

  private var chartData = emptyArray[js.Object]
  private val colors = js.Array("#00ff00", "#88ffff", "#8888ff", "#ff8000", "#88ffaa", "#ff88ff", "#ff8888")
  private val exposures = js.Array(
    Exposure(value = "sector", label = "Sector Exposure"),
    Exposure(value = "industry", label = "Industry Exposure"),
    Exposure(value = "exchange", label = "Exchange Exposure"),
    Exposure(value = "market", label = "Exchange Sub-Market Exposure"),
    Exposure(value = "securities", label = "Securities Exposure"))

  ///////////////////////////////////////////////////////////////////////////
  //          Public Variables
  ///////////////////////////////////////////////////////////////////////////

  $scope.selectedExposure = js.undefined

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getExposures = () => exposures

  $scope.getChartData = () => chartData

  $scope.exposurePieChart = (exposure: js.UndefOr[String], contestID: js.UndefOr[String], userID: js.UndefOr[String]) => {
    for {
      cid <- contestID
      uid <- userID
      eid <- exposure
    } exposurePieChart(BSONObjectID(cid), BSONObjectID(uid), eid)
  }

  $scope.colorFunction = () => { (d: Exposure, i: Double) => colors(i.toInt % colors.length) }: js.Function2[Exposure, Double, String]

  $scope.xFunction = () => { (d: Exposure) => d.label }: js.Function1[Exposure, String]

  $scope.yFunction = () => { (d: Exposure) => d.value }: js.Function1[Exposure, String]

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def exposurePieChart(aContestID: js.UndefOr[BSONObjectID], aUserID: js.UndefOr[BSONObjectID], anExposure: js.UndefOr[String]) = {
    for {
      contestID <- aContestID
      userID <- aUserID
      exposure <- anExposure
    } {
      contestService.getExposureChartData(contestID, userID, exposure) onComplete {
        case Success(data) => chartData = data
        case Failure(e) =>
          console.error(s"Failed to load exposure data for $exposure")
      }
    }
  }

}

/**
  * Exposure Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ExposureScope extends Scope {
  // variables
  var selectedExposure: js.UndefOr[String]

  // graph functions
  var colorFunction: js.Function0[js.Function2[Exposure, Double, String]]
  var xFunction: js.Function0[js.Function1[Exposure, String]]
  var yFunction: js.Function0[js.Function1[Exposure, String]]

  // accessors
  var exposurePieChart: js.Function3[js.UndefOr[String], js.UndefOr[String], js.UndefOr[String], Unit]
  var getChartData: js.Function0[js.Array[js.Object]]
  var getExposures: js.Function0[js.Array[Exposure]]

}

/**
  * Exposure Model
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Exposure extends js.Object {
  var label: String = js.native
  var value: String = js.native
}

/**
  * Exposure Model Companion Object
  * @author lawrence.daniels@gmail.com
  */
object Exposure {

  def apply(label: String, value: String) = {
    val model = New[Exposure]
    model.label = label
    model.value = value
    model
  }

}