package com.shocktrade.client.proxies

import com.shocktrade.common.forms.{ContestCreationRequest, ContestSearchForm}
import com.shocktrade.remote.proxies.ContestProxy
import io.scalajs.JSON
import org.scalatest.funspec.AsyncFunSpec

import scala.concurrent.ExecutionContextExecutor
import scala.scalajs.concurrent.JSExecutionContext

/**
 * Contest Proxy Test Suite
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestProxyTest extends AsyncFunSpec {
  implicit override val executionContext: ExecutionContextExecutor = JSExecutionContext.queue
  private val contestProxy = new ContestProxy("localhost", 9000)

  describe(classOf[ContestProxy].getSimpleName) {

    it("should retrieve contests") {
      contestProxy.contestSearch(new ContestSearchForm()) map { searchResults =>
        info(s"${searchResults.length} contests retrieved")
        assert(searchResults.nonEmpty)
      }
    }

    it("should create new contests") {
      val outcome = contestProxy.createNewGame(new ContestCreationRequest(
        userID = "af3480c6-72d5-11ea-83ac-857ee918b853",
        name = s"It's time to play!",
        perksAllowed = true,
        robotsAllowed = true,
        startAutomatically = true,
        startingBalance = 2500.0,
        duration = 1
      ))

      outcome map { response =>
        info(s"response: ${JSON.stringify(response)}")
        assert(response.contestID.nonEmpty)
      }
    }

  }

}
