package com.shocktrade.client

import com.shocktrade.client.discover.{MarketStatus, MarketStatusService}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope, Timeout}
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

  def $scope: T

  def $timeout: Timeout

  def marketStatusService: MarketStatusService

  def toaster: Toaster

  $scope.isUSMarketsOpen = () => {
    usMarketStatus match {
      case Left(status) => Option(status.active).orUndefined
      case Right(loading) =>
        if (!loading) {
          usMarketStatus = Right(true)
          console.log("Retrieving market status...")
          marketStatusService.getMarketStatus onComplete {
            case Success(response) =>
              val status = response.data

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

}

/**
 * U.S. Markets Status Support Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait USMarketsStatusSupportScope extends Scope {
  var isUSMarketsOpen: js.Function0[js.UndefOr[Boolean]] = js.native

}