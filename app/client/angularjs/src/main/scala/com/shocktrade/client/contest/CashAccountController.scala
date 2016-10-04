package com.shocktrade.client.contest

import com.shocktrade.client.MySessionService
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Controller, Scope, injected}
import org.scalajs.sjs.JsUnderOrHelper._

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

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

  $scope.asOfDate = () => mySession.cashAccount_?.flatMap(a => a.asOfDate.toOption) getOrElse new js.Date()

  $scope.getTotalOrders = () => Seq("BUY", "SELL") map computeTotalOrdersByType sum

  $scope.getTotalEquity = () => $scope.getTotalInvestment() + $scope.getFundsAvailable()

  $scope.getTotalBuyOrders = () => computeTotalOrdersByType(orderType = "BUY")

  $scope.getTotalSellOrders = () => computeTotalOrdersByType(orderType = "SELL")

  $scope.getFundsAvailable = () => mySession.cashAccount_?.orUndefined.flatMap(_.cashFunds) getOrElse 0d

  $scope.getTotalInvestment = () => {
    val outcome = for {
      portfolio <- mySession.portfolio_?.toList
      positions <- portfolio.positions.toList
      cashPositions = positions.filter(_.isCashAccount)
    } yield cashPositions.map(_.netValue.getOrElse(0d)).sum
    outcome.sum
  }

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def computeTotalOrdersByType(orderType: String) = {
    val outcome = for {
      portfolio <- mySession.portfolio_?.toList
      orders <- portfolio.orders.toList
      cashOrders = orders.filter(o => o.orderType.contains(orderType) && o.isCashAccount)
    } yield cashOrders.map(_.totalCost.getOrElse(0d)).sum
    outcome.sum
  }

}

/**
  * Cash Account Controller Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait CashAccountScope extends Scope {
  var asOfDate: js.Function0[js.Date] = js.native
  var getTotalOrders: js.Function0[Double] = js.native
  var getTotalEquity: js.Function0[Double] = js.native
  var getTotalBuyOrders: js.Function0[Double] = js.native
  var getTotalSellOrders: js.Function0[Double] = js.native
  var getFundsAvailable: js.Function0[Double] = js.native
  var getTotalInvestment: js.Function0[Double] = js.native

}