package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.{Controller, Scope, injected}
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
  // functions
  var asOfDate: js.Function0[js.Date]
  var getTotalOrders: js.Function0[Double]
  var getTotalEquity: js.Function0[Double]
  var getTotalBuyOrders: js.Function0[Double]
  var getTotalSellOrders: js.Function0[Double]
  var getFundsAvailable: js.Function0[Double]
  var getTotalInvestment: js.Function0[Double]

}