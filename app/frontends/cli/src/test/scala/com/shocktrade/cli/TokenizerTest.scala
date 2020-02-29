package com.shocktrade.cli

import org.scalatest.{FreeSpec, Matchers}

/**
  * Tokenizer Tests
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class TokenizerTest extends FreeSpec with Matchers {

  "parseFully() should parse text into tokens" - {

    "f(x)=3x+2 is parsed into components" in {
      val tokenizer = new Tokenizer()
      val tokens = tokenizer.parseFully("f(x)=3x+2")
      tokens shouldBe List("f", "(", "x", ")", "=", "3", "x", "+", "2")
    }

  }

}
