package com.shocktrade.client.contest

import com.shocktrade.client.MySessionService
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.OptionHelper._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Margin Account Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class MarginAccountController($scope: MarginAccountScope, $timeout: Timeout, toaster: Toaster,
                              @injected("ContestService") contestService: ContestService,
                              @injected("MySessionService") mySession: MySessionService,
                              @injected("PortfolioService") portfolioService: PortfolioService) extends Controller {

  private val interestRate = 0.15
  private val initialMargin = 0.50
  private val maintenanceMargin = 0.25
  private var investmentMarketValue = 0.0d

  /////////////////////////////////////////////////////////////////////
  //          Initialization Function
  /////////////////////////////////////////////////////////////////////

  private var attemptsLeft = 3

  $scope.initMarginAccount = () => {
    investmentMarketValue = investmentCost

    for {
      portfolioID <- mySession.portfolio_?.flatMap(_._id.toOption)
    } {
      // load the margin accounts market value
      portfolioService.getMarginAccountMarketValue(portfolioID) onComplete {
        case Success(response) =>
          investmentMarketValue = response.marketValue
        case Failure(e) =>
          toaster.error("Failed to retrieve the Margin Account's market value")
          attemptsLeft -= 1
          if (attemptsLeft > 0) $timeout(() => $scope.initMarginAccount(), 5.seconds)
      }
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Public Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getAsOfDate = () => mySession.marginAccount_?.flatMap(_.asOfDate.toOption) getOrElse new js.Date()

  $scope.getBuyingPower = () => funds / initialMargin

  $scope.getCashFunds = () => funds

  $scope.getInterestPaid = () => mySession.marginAccount_?.flatMap(_.interestPaid.toOption) orZero

  $scope.getInterestRate = () => interestRate

  $scope.getInitialMargin = () => initialMargin

  $scope.getMaintenanceMargin = () => maintenanceMargin

  $scope.getInvestmentCost = () => investmentCost

  $scope.getInvestmentMarketValue = () => investmentMarketValue

  $scope.isAccountInGoodStanding = () => funds >= maintenanceMarginAmount

  $scope.getMarginAccountEquity = () => marginAccountEquity

  $scope.getMaintenanceMarginAmount = () => maintenanceMarginAmount

  // TODO round to nearest penny
  $scope.getMarginCallAmount = () => maintenanceMarginAmount - funds

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def funds = mySession.marginAccount_?.flatMap(_.funds.toOption) orZero

  private def investmentCost = {
    val outcome = for {
      portfolio <- mySession.portfolio_?.toList
      positions <- portfolio.positions.toList
      marginPositions = positions.filter(_.isMarginAccount)
    } yield marginPositions.map(_.totalCost.orZero).sum
    outcome.sum
  }

  private def marginAccountEquity = {
    val myInvestmentCost = investmentCost
    funds + (Math.max(investmentMarketValue, myInvestmentCost) - myInvestmentCost)
  }

  private def maintenanceMarginAmount = {
    val maintenanceAmount = (investmentCost - marginAccountEquity) * maintenanceMargin
    if (maintenanceAmount > 0) maintenanceAmount else 0.0d
  }

}

/**
  * Margin Account Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait MarginAccountScope extends Scope {
  // functions
  var initMarginAccount: js.Function0[Unit] = js.native
  var getAsOfDate: js.Function0[js.Date] = js.native
  var getBuyingPower: js.Function0[Double] = js.native
  var getCashFunds: js.Function0[Double] = js.native
  var getInterestPaid: js.Function0[Double] = js.native
  var getInterestRate: js.Function0[Double] = js.native
  var getInitialMargin: js.Function0[Double] = js.native
  var getMaintenanceMargin: js.Function0[Double] = js.native
  var getInvestmentCost: js.Function0[Double] = js.native
  var getInvestmentMarketValue: js.Function0[Double] = js.native
  var isAccountInGoodStanding: js.Function0[Boolean] = js.native
  var getMarginAccountEquity: js.Function0[Double] = js.native
  var getMaintenanceMarginAmount: js.Function0[Double] = js.native
  var getMarginCallAmount: js.Function0[Double] = js.native

}
