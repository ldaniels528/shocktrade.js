package com.shocktrade.serverside.persistence

import com.shocktrade.eventsource.EventSource.generateUID
import com.shocktrade.eventsource.FileEventSource
import com.shocktrade.serverside.persistence.dao.{OrderDAO, PositionDAO, UserDAO}
import com.shocktrade.serverside.persistence.eventsource._
import io.scalajs.JSON
import io.scalajs.nodejs.setImmediate
import org.scalatest.FunSpec

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

/**
  * Qualification Server Tests
  * @author lawrence.daniels@gmail.com
  */
class QualificationServerTest extends FunSpec {
  // define the event source
  val eventSource = new FileEventSource("./events.json")

  // create the DAOs
  val orderDAO = OrderDAO()
  val positionDAO = PositionDAO()
  val userDAO = UserDAO()

  describe("QualificationServer") {

    it("should notify subscribers of new events and replay events") {
      // define the events counter
      var count = 0
      eventSource.register { event =>
        info(s"event: ${JSON.stringify(event)}")
        count += 1
      }

      userDAO.findByName("ldaniels") foreach (_ foreach { user =>
        info(s"Using user: ${JSON.stringify(user)}")

        val orderID = generateUID
        val userID = user.userID

        info("Creating events...")
        eventSource.add(new OrderCreationEvent(
          userID = user.userID,
          orderID = orderID,
          orderType = OrderTypes.Buy,
          symbol = "AAPL",
          exchange = "NASDAQ",
          price = js.undefined,
          priceType = PriceTypes.Market,
          quantity = 150
        ))
        eventSource.add(new OrderUpdateEvent(userID = userID, orderID = orderID, price = 170.00, priceType = PriceTypes.Limit, quantity = 100))
        eventSource.add(new OrderCloseEvent(userID = userID, orderID = orderID, fulfilled = false, userInitiated = true))

        // playback the events
        setImmediate { () =>
          info(s"event count was $count")
          assert(count == 3)
          eventSource.replay(QualificationServer.handleEvent)
        }
      })
    }

  }

}
