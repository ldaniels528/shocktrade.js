package com.shocktrade.javascript.dashboard

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.core.Controller
import com.ldaniels528.javascript.angularjs.extensions.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.Date

/**
 * Cash Account Controller
 * @author lawrence.daniels@gmail.com
 */
class CashAccountController($scope: js.Dynamic, toaster: Toaster, @named("MySession") mySession: MySession) extends Controller {

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.asOfDate = () => mySession.cashAccount_?.flatMap(a => Option(a.asOfDate)) getOrElse new Date()

  $scope.getFundsAvailable = () => getFundsAvailable

  $scope.getTotalOrders = () => Seq("BUY", "SELL") map getTotalOrdersByType sum

  $scope.getTotalEquity = () => getTotalInvestment + getFundsAvailable

  $scope.getTotalInvestment = () => getTotalInvestment

  $scope.getTotalBuyOrders = () => getTotalOrdersByType(orderType = "BUY")

  $scope.getTotalSellOrders = () => getTotalOrdersByType(orderType = "SELL")

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def getFundsAvailable = mySession.cashAccount_?.flatMap(a => Option(a.cashFunds)).map(_.as[Double]).getOrElse(0d)

  private def getTotalInvestment = {
    var total = 0d
    mySession.participant foreach (_.positions.asArray[js.Dynamic] filter (_.accountType === "CASH") foreach (total += _.netValue.as[Double]))
    total
  }

  private def getTotalOrdersByType(orderType: String) = {
    var total = 0d
    mySession.participant foreach (_.orders.asArray[js.Dynamic] filter (o => o.orderType === orderType && o.accountType === "CASH") foreach { o =>
      total += o.price.as[Double] * o.quantity.as[Double] + o.commission.as[Double]
    })
    total
  }

}
