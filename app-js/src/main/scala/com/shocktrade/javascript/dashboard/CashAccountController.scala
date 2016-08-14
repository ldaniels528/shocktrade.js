package com.shocktrade.javascript.dashboard

import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Controller, Scope, injected}
import com.shocktrade.javascript.MySessionService

import scala.language.postfixOps
import scala.scalajs.js

/**
  * Cash Account Controller
  * @author lawrence.daniels@gmail.com
  */
class CashAccountController($scope: CashAccountScope, toaster: Toaster,
                            @injected("MySessionService") mySession: MySessionService)
  extends Controller {

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.asOfDate = () => mySession.cashAccount_?.flatMap(a => Option(a.asOfDate)) getOrElse new js.Date()

  $scope.getTotalOrders = () => Seq("BUY", "SELL") map computeTotalOrdersByType sum

  $scope.getTotalEquity = () => $scope.getTotalInvestment() + $scope.getFundsAvailable()

  $scope.getTotalBuyOrders = () => computeTotalOrdersByType(orderType = "BUY")

  $scope.getTotalSellOrders = () => computeTotalOrdersByType(orderType = "SELL")

  $scope.getFundsAvailable = () => mySession.cashAccount_?.map(_.cashFunds) getOrElse 0d

  $scope.getTotalInvestment = () => {
    mySession.participant.map(_.positions.filter(_.accountType == "CASH")).map(_ map (_.netValue) sum) getOrElse 0d
  }

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def computeTotalOrdersByType(orderType: String) = {
    var total = 0d
    mySession.participant foreach (_.orders filter (o => o.orderType == orderType && o.accountType == "CASH") foreach { o =>
      total += o.price * o.quantity + o.commission
    })
    total
  }

}

/**
  * Cash Account Controller Scope
  * @author lawrence.daniels@gmail.com
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