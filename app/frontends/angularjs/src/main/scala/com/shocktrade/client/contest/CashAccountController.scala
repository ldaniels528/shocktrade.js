package com.shocktrade.client.contest

import com.shocktrade.client.models.contest.Portfolio
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope}
import io.scalajs.util.JsUnderOrHelper._

import scala.language.postfixOps
import scala.scalajs.js

/**
 * Cash Account Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class CashAccountController($scope: CashAccountScope, toaster: Toaster) extends Controller {

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.asOfDate = () => new js.Date()

  $scope.getFundsAvailable = () => $scope.portfolio.flatMap(_.funds)

  $scope.getTotalOrders = () => Seq("BUY", "SELL") map computeTotalOrdersByType sum

  $scope.getTotalEquity = () => $scope.getTotalInvestment() + $scope.getFundsAvailable().orZero

  $scope.getTotalBuyOrders = () => computeTotalOrdersByType(orderType = "BUY")

  $scope.getTotalSellOrders = () => computeTotalOrdersByType(orderType = "SELL")

  $scope.getTotalInvestment = () => {
    val outcome = for {
      portfolio <- $scope.portfolio.toList
      positions <- portfolio.positions.toList
      cashPositions = positions
    } yield cashPositions.map(_.netValue.orZero).sum
    outcome.sum
  }

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def computeTotalOrdersByType(orderType: String): Double = {
    val outcome = for {
      portfolio <- $scope.portfolio.toList
      orders <- portfolio.orders.toList
      cashOrders = orders.filter(o => o.orderType.contains(orderType))
    } yield cashOrders.map(_.totalCost.orZero).sum
    outcome.sum
  }

}

/**
 * Cash Account Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait CashAccountScope extends Scope {
  // variables
  var portfolio: js.UndefOr[Portfolio] = js.native

  // functions
  var asOfDate: js.Function0[js.Date] = js.native
  var getFundsAvailable: js.Function0[js.UndefOr[Double]] = js.native
  var getTotalOrders: js.Function0[Double] = js.native
  var getTotalEquity: js.Function0[Double] = js.native
  var getTotalBuyOrders: js.Function0[Double] = js.native
  var getTotalSellOrders: js.Function0[Double] = js.native
  var getTotalInvestment: js.Function0[Double] = js.native

}