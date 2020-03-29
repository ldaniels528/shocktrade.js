package com.shocktrade.client

import com.shocktrade.client.discover.{MarketStatus, MarketStatusService}
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope, Timeout}
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * U.S. Markets Status Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait USMarketsStatusSupport[T <: USMarketsStatusSupportScope] {
  ref: Controller =>

  private var usMarketStatus: Either[MarketStatus, Boolean] = Right(false)
  private var lastContestID: js.UndefOr[String] = js.undefined

  def $scope: T

  def $timeout: Timeout

  def marketStatusService: MarketStatusService

  def toaster: Toaster

  $scope.isUSMarketsOpen = (aContestID: js.UndefOr[String]) => {
    val contestID = aContestID ?? lastContestID
    usMarketStatus match {
      case Left(status) => Option(status.active).orUndefined
      case Right(loading) =>
        if (!loading) {
          usMarketStatus = Right(true)
          console.log(s"Retrieving market status${contestID.map(id => s" for contest $id") getOrElse ""}...")
          val outcome = contestID.map(marketStatusService.getMarketStatus(_)) getOrElse marketStatusService.getMarketStatus
          outcome onComplete {
            case Success(response) =>
              val status = response.data
              console.info(JSON.stringify(status))

              // capture the current status
              $scope.$apply(() => usMarketStatus = Left(status))

              // update the status after delay
              $timeout(() => usMarketStatus = Right(false), status.delay.toInt)

            case Failure(e) =>
              toaster.error("Failed to retrieve market status")
              console.error(s"Failed to retrieve market status: ${e.getMessage}")
          }
        }
        js.undefined
    }
  }

  $scope.resetMarketStatus = (contestID: js.UndefOr[String]) => {
    console.log(s"Resetting market status${contestID.map(id => s" for contest $id") getOrElse ""}...")
    lastContestID = contestID
    usMarketStatus = Right(false)
  }

}

/**
 * U.S. Markets Status Support Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait USMarketsStatusSupportScope extends Scope {
  var isUSMarketsOpen: js.Function1[js.UndefOr[String], js.UndefOr[Boolean]] = js.native
  var resetMarketStatus: js.Function1[js.UndefOr[String], Unit] = js.native

}