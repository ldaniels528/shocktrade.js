package com.shocktrade.webapp.vm.proccesses.cqm

import com.shocktrade.common.forms.ContestCreationRequest
import com.shocktrade.webapp.vm.opcodes.{CreateContest, OpCode}
import com.shocktrade.webapp.vm.proccesses.cqm.dao.{ContestExpiredData, OrderExpiredData, QualifiedOrderData}
import org.scalatest.funspec.AnyFunSpec

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

/**
 * Contest Qualification Module Test
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestQualificationModuleTest extends AnyFunSpec {
  private val oqm = new ContestQualificationModule()

  describe(classOf[ContestQualificationModule].getSimpleName) {

    it("should close expired contests") {
      val contests = js.Array(
        new ContestExpiredData(contestID = "contest000", userID = "user000", portfolioID = "portfolio000", funds = 2865.0),
        new ContestExpiredData(contestID = "contest000", userID = "user001", portfolioID = "portfolio001", funds = 5379.0)
      )
      val opCodes = oqm.processContestClosedEvents(contests)
      show(opCodes)
    }

    it("should close expired orders") {
      val orders = js.Array(
        new OrderExpiredData(portfolioID = "portfolio000", orderID = "order000"),
        new OrderExpiredData(portfolioID = "portfolio001", orderID = "order001")
      )
      val opCodes = oqm.processOrderCloseEvents(orders)
      show(opCodes)
    }

    it("should create positions from orders") {
      val orders = js.Array(
        new QualifiedOrderData(
          contestID = "contest000",
          userID = "user000",
          portfolioID = "portfolio000",
          orderID = "order000",
          symbol = "QST",
          exchange = "OTCBB",
          orderType = "BUY",
          priceType = "LIMIT",
          price = 0.67,
          quantity = 5000.0,
          lastTrade = 0.66,
          tradeDateTime = new js.Date(),
          cost = 3300.0,
          funds = 25000.0,
          creationTime = new js.Date(),
          expirationTime = new js.Date()
        ),
        new QualifiedOrderData(
          contestID = "contest000",
          userID = "user000",
          portfolioID = "portfolio000",
          orderID = "order001",
          symbol = "BSB",
          exchange = "OTCBB",
          orderType = "BUY",
          priceType = "LIMIT",
          price = 0.011,
          quantity = 500000.0,
          lastTrade = 0.011,
          tradeDateTime = new js.Date(),
          cost = 5500.0,
          funds = 25000.0,
          creationTime = new js.Date(),
          expirationTime = new js.Date()
        ),
        new QualifiedOrderData(
          contestID = "contest000",
          userID = "user000",
          portfolioID = "portfolio000",
          orderID = "order001",
          symbol = "BSB",
          exchange = "OTCBB",
          orderType = "SELL",
          priceType = "LIMIT",
          price = 0.013,
          quantity = 500000.0,
          lastTrade = 0.011,
          tradeDateTime = new js.Date(),
          cost = 5500.0,
          funds = 25000.0,
          creationTime = new js.Date(),
          expirationTime = new js.Date()
        ))
      val opCodes = oqm.processOrders(orders)
      show(opCodes)
    }

    it("should orchestrate the contest life-cycle") {
      val opCodes = js.Array(
        CreateContest(new ContestCreationRequest(
          contestID = js.undefined,
          name = "This is it",
          userID = "af3480c6-72d5-11ea-83ac-857ee918b853",
          startingBalance = 25000.0,
          startAutomatically = true,
          duration = 1,
          friendsOnly = false,
          invitationOnly = false,
          levelCapAllowed = false,
          levelCap = js.undefined,
          perksAllowed = false,
          robotsAllowed = false
        ))
      )
    }

  }

  def show(opCodes: js.Array[OpCode]): Unit = {
    info(s"${opCodes.length} opCode(s) found:")
    opCodes foreach (i => info(i.toString))
  }

}
