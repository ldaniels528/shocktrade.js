package com.shocktrade.client.proxies

import com.shocktrade.remote.loader.Scope
import org.scalatest.funspec.AnyFunSpec

/**
 * Scope Tests
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ScopeTest extends AnyFunSpec {

  describe(classOf[Scope].getSimpleName) {

    it("should identify variable references") {
      val line = """putChatMessage $$1.contestID { "userID":"$$2.userID", "username":"fugitive528", "message":"Hello World" }"""
      val results = Scope.findVariables(line, correlationID = "1")
      info(s"reference: ${results.mkString(",")}")
      info(s"indices: ${results.map(_.getInstanceKey).mkString(",")}")
      assert(results.size == 2)
    }

  }

}
