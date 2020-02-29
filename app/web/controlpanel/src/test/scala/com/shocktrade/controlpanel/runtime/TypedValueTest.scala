package com.shocktrade.controlpanel.runtime

import org.scalatest.{FreeSpec, Matchers}

/**
  * Typed Value Test
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class TypedValueTest extends FreeSpec with Matchers {

  "TypedValues should evaluate mathematically" - {

    "5 + 7 = 12" in {
      val x = NumericValue(5)
      val y = NumericValue(7)
      x + y shouldBe NumericValue(12)
    }

    "5 - 7 = -2" in {
      val x = NumericValue(5)
      val y = NumericValue(7)
      x - y shouldBe NumericValue(-2)
    }

    "5 * 7 = 35" in {
      val x = NumericValue(5)
      val y = NumericValue(7)
      x * y shouldBe NumericValue(35)
    }

    "5 / 7 = 0.7142857" in {
      val x = NumericValue(5)
      val y = NumericValue(7)
      x / y shouldBe NumericValue(5.0 / 7.0)
    }

  }

}
