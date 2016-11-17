package com.shocktrade.controlpanel

import com.shocktrade.controlpanel.runtime.ExitOp

/**
  * Shocktrade script compiler
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class Compiler(tokenizer: Tokenizer) {

  def compile(script: String) = {
    decompose(new HLParser(tokenizer.parse(script)))
  }

  def decompose(hl: HLParser) = {
    hl.next() match {
      case "exit" =>
        if (hl.peek.contains("(")) parameters(hl)
        new ExitOp()
      case unknown =>
        throw new IllegalArgumentException(s"Syntax error near '$unknown'")
    }
  }

  def parameters(hl: HLParser) = expression(hl.extractParams())

  def expression(args: List[String]) = {
    println(s"args = ${args.mkString(" | ")}")
  }

}

/**
  * Compiler Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Compiler {

  def apply() = {
    new Compiler(new Tokenizer())
  }

}