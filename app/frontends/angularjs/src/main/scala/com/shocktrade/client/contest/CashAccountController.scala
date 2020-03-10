package com.shocktrade.client.contest

import com.shocktrade.client.{MySessionService, RootScope}
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, injected}
import io.scalajs.util.JsUnderOrHelper._

import scala.language.postfixOps
import scala.scalajs.js

/**
 * Cash Account Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class CashAccountController($scope: CashAccountScope, toaster: Toaster,
                            @injected("MySessionService") mySession: MySessionService)
  extends Controller {

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.asOfDate = () => new js.Date()

  $scope.getTotalOrders = () => Seq("BUY", "SELL") map computeTotalOrdersByType sum

  $scope.getTotalEquity = () => $scope.getTotalInvestment() + $scope.getFundsAvailable().orZero

  $scope.getTotalBuyOrders = () => computeTotalOrdersByType(orderType = "BUY")

  $scope.getTotalSellOrders = () => computeTotalOrdersByType(orderType = "SELL")

  $scope.getTotalInvestment = () => {
    val outcome = for {
      portfolio <- mySession.portfolio_?.toList
      positions <- portfolio.positions.toList
      cashPositions = positions.filter(_.isCashAccount)
    } yield cashPositions.map(_.netValue.orZero).sum
    outcome.sum
  }

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def computeTotalOrdersByType(orderType: String): Double = {
    val outcome = for {
      portfolio <- mySession.portfolio_?.toList
      orders <- portfolio.orders.toList
      cashOrders = orders.filter(o => o.orderType.contains(orderType) && o.isCashAccount)
    } yield cashOrders.map(_.totalCost.orZero).sum
    outcome.sum
  }

}

/**
 * Cash Account Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait CashAccountScope extends RootScope {
  var asOfDate: js.Function0[js.Date] = js.native
  var getTotalOrders: js.Function0[Double] = js.native
  var getTotalEquity: js.Function0[Double] = js.native
  var getTotalBuyOrders: js.Function0[Double] = js.native
  var getTotalSellOrders: js.Function0[Double] = js.native
  var getTotalInvestment: js.Function0[Double] = js.native

}