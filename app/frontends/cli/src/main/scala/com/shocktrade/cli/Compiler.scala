package com.shocktrade.cli

import com.shocktrade.cli.Compiler._
import com.shocktrade.cli.Tokenizer.{TokenEnrichment, TokenIterator}
import com.shocktrade.cli.runtime._
import com.shocktrade.cli.runtime.functions.builtin.BuiltinFunctions
import com.shocktrade.cli.runtime.ops.{DefineFunctionOp, DefineVariableOp, MathOp, ScopeOp}
import com.shocktrade.cli.runtime.values.{ArrayReference, FunctionReference, VariableReference}

/**
 * ShockScript Compiler
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Compiler(tokenizer: Tokenizer) {

  /**
   * Compiles the given script into executable code
   * @param script the given script
   * @return the [[CodeBlock executable code]]
   */
  def compile(script: String): Evaluatable = {
    var list: List[Evaluatable] = Nil
    val it = tokenizer.parse(script)
    while (it.hasNext) {
      list = expression(it) :: list
    }
    new CodeBlock(ops = list.reverse)
  }

  /**
   * Parses an expression (e.g. "10 * (5 + 1)")
   * @param it the given [[TokenIterator token iterator]]
   * @return an [[Evaluatable evaluatable]] representing the quantity
   */
  def expression(it: TokenIterator): Evaluatable = {
    var previous = it.peek match {
      case "$" => variableReference(it)
      case "[" => arrayReference(it)
      case "(" => quantity(it)
      case "def" => defineFunction(it)
      case "scope" => scopeOp(it)
      case "var" => defineVariable(it)
      case value if value.isConstant => toConstantValue(it.next())
      case name if BuiltinFunctions.contains(name) => functionReference(it)
      case unknown =>
        throw new IllegalArgumentException(s"Syntax error near '$unknown'")
    }

    // use aggregation for mathematical operators
    while (it.peekOption.exists(_.isOperator)) {
      previous = new MathOp(operator = it.next(), previous, expression(it))
    }
    previous
  }

  /**
   * Parses an array reference (e.g. "[1, 2, 3]")
   * @param it the given [[TokenIterator token iterator]]
   * @return the [[ArrayReference array reference]]
   */
  def arrayReference(it: TokenIterator): ArrayReference = {
    it.expect("[")
    var items: List[Evaluatable] = Nil
    do {
      if (!it.peekOption.contains("]")) items = expression(it) :: items
    } while (it.expect(",", "]") != "]")
    new ArrayReference(items.reverse)
  }

  /**
   * Parses a function declaration (e.g. "def combine(a, b) = $a + $b")
   * @param it the given [[TokenIterator token iterator]]
   * @return the [[DefineFunctionOp function definition]]
   */
  def defineFunction(it: TokenIterator): DefineFunctionOp = {
    it.next() // skip "def" keyword
    val name = it.next()

    // gather the parameters
    it.expect("(")
    var params: List[String] = Nil
    do {
      if (!it.peekOption.contains(")")) params = it.next() :: params
    } while (it.expect(",", ")") != ")")

    // define the function body
    it.expect("=")
    new DefineFunctionOp(name, params.reverse, expression(it))
  }

  /**
   * Parses a variable declaration (e.g. "var array = [1, 2, 3]")
   * @param it the given [[TokenIterator token iterator]]
   * @return the [[DefineVariableOp variable definition]]
   */
  def defineVariable(it: TokenIterator): DefineVariableOp = {
    it.next() // skip "var" keyword
    val name = it.next()
    it.expect("=")
    new DefineVariableOp(name, expression(it))
  }

  /**
   * Parses a function reference (e.g. "sum(1, 2, 3)")
   * @param it the given [[TokenIterator token iterator]]
   * @return the [[FunctionReference function reference]]
   */
  def functionReference(it: TokenIterator): FunctionReference = {
    val name = it.next()
    it.expect("(")
    var args: List[Evaluatable] = Nil
    do {
      if (!it.peekOption.contains(")")) args = expression(it) :: args
    } while (it.expect(",", ")") != ")")
    new FunctionReference(name, args.reverse)
  }

  /**
   * Parses a quantity (e.g. "(1 + 2) * 3")
   * @param it the given [[TokenIterator token iterator]]
   * @return an [[Evaluatable evaluatable]] representing the quantity
   */
  def quantity(it: TokenIterator): Evaluatable = {
    it.expect("(")
    var aggregate = expression(it)
    while (it.hasNext && it.peek != ")") {
      aggregate = it.next() match {
        case operator if operator.isOperator => new MathOp(operator, aggregate, expression(it))
        case unknown =>
          throw new IllegalArgumentException(s"Syntax error near '$unknown'")
      }
    }
    it.expect(")")
    aggregate
  }

  def scopeOp(it: TokenIterator): ScopeOp = {
    it.next()
    new ScopeOp()
  }

  /**
   * Parses a variable reference (e.g. "$a")
   * @param it the given [[TokenIterator token iterator]]
   * @return a [[VariableReference variable reference]]
   */
  def variableReference(it: TokenIterator): VariableReference = {
    it.next() // skip variable reference symbol ($)
    new VariableReference(name = it.next())
  }

  private def toConstantValue(value: String): Evaluatable = {
    value match {
      case s if s.isNull => Null
      case s if s.isNumeric => NumericValue(s.toDouble)
      case s if s.isQuoted => TextValue(s.drop(1).dropRight(1))
      case s => throw new IllegalArgumentException(s"Unrecognized constant value '$s'")
    }
  }

}

/**
 * Compiler Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Compiler {

  /**
   * Default Constructor
   * @return a new Compiler instance using a default tokenizer
   */
  def apply(): Compiler = {
    new Compiler(new Tokenizer())
  }

  /**
   * Tokenizer Extensions
   * @param it the given [[TokenIterator token iterator]]
   */
  final implicit class TokenizerExtensions(val it: TokenIterator) extends AnyVal {

    def assert(f: TokenIterator => Boolean): String = {
      it.peekOption match {
        case Some(token) =>
          if (!f(it)) throw new IllegalArgumentException("Assertion failure")
          token
        case None =>
          throw new IllegalArgumentException(s"Unexpected end of input")
      }
    }

    def peekOption: Option[String] = if (it.hasNext) Some(it.peek) else None

    def expect(tokens: String*): String = {
      if (!it.hasNext)
        throw new IllegalArgumentException(s"Unexpected end of input: expected ${tokens.mkString(" or ")} ")

      val actual = it.next()
      if (!tokens.contains(actual)) {
        throw new IllegalArgumentException(s"Expected ${tokens.mkString(" or ")} near '$actual'")
      }
      actual
    }

    def nextOption(): Option[String] = if (it.hasNext) Option(it.next()) else None

  }

}