package com.shocktrade.autonomous

import com.shocktrade.autonomous.RuleCompiler._
import com.shocktrade.autonomous.RuleProcessor._
import com.shocktrade.autonomous.dao.{BuyingFlow, SellingFlow}
import com.shocktrade.common.models.quote.ResearchQuote
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.nodejs.util.Util
import org.scalajs.nodejs.{NodeRequire, console}
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.js

/**
  * Rule Compiler
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RuleCompiler()(implicit require: NodeRequire) {
  private implicit val moment = Moment()
  private implicit val util = Util()

  /**
    * Compiles the given strategy into executable op-codes
    * @param buyingFlow the given [[BuyingFlow buying flow]]
    * @param env        the given [[RobotEnvironment robot environment]]
    * @return the resulting [[OpCode op-codes]]
    */
  def apply(buyingFlow: BuyingFlow)(implicit env: RobotEnvironment) = {
    for {
      rule <- buyingFlow.rules getOrElse emptyArray
      name <- rule.name.toList
      ops <- rule.exclude getOrElse emptyArray
      (key, value) <- ops
    } yield decode(name, key, value)
  }

  /**
    * Compiles the given strategy into executable op-codes
    * @param sellingFlow the given [[SellingFlow selling flow]]
    * @param env         the given [[RobotEnvironment robot environment]]
    * @return the resulting [[OpCode op-codes]]
    */
  def apply(sellingFlow: SellingFlow)(implicit env: RobotEnvironment) = emptyArray[OpCode]

  @inline
  private def decode(name: String, field: String, value: js.Any)(implicit env: RobotEnvironment) = {
    //console.log("%s => %j [string]", field, value)
    field match {
      case "advisory" => withAdvisory(name, value)
      case "exchange" => withExchange(name, value)
      case "symbol" => withSymbol(name, value)
      case _ =>
        console.error(s"Invalid field '$field'"); withNothing(name, value)
    }
  }

  @inline
  private def withAdvisory(name: String, value: js.Any) = new OpCode(name, (r: ResearchQuote) => value match {
    case s if s.is("INFO") => r.getAdvisory.exists(_.isInformational)
    case s if s.is("WARNING") => r.getAdvisory.exists(_.isWarning)
    case v => console.error(s"advisory: Invalid advisory - $v"); false
  })

  @inline
  private def withExchange(name: String, value: js.Any) = {
    new OpCode(name, (r: ResearchQuote) => r.exchange.exists(_ == value))
  }

  @inline
  private def withSymbol(name: String, value: js.Any)(implicit env: RobotEnvironment) = {
    new OpCode(name, (r: ResearchQuote) => value match {
      // symbol LIKE 'APP'
      case v if util.isString(v) => (for (symbol <- r.symbol; s <- v.asString) yield symbol.startsWith(s)).isTrue

      // symbol IN [positions, orders]
      case v if util.isObject(v) => decodeIN(r, v).isTrue

      // unknown
      case v => console.error(s"symbol: Invalid value - %j", v); false
    })
  }

  @inline
  private def withNothing(name: String, value: js.Any) = new OpCode(name, (r: ResearchQuote) => false)

  /**
    * IN [...]
    * @param r the given [[ResearchQuote research quote]]
    * @param v the given value
    * @return the result of the evaluated expression
    */
  private def decodeIN(r: ResearchQuote, v: js.Any)(implicit env: RobotEnvironment) = {
    for {
      inOp <- v.as[InOp]
      array <- inOp.in
    } yield {
      array exists {
        case x if util.isString(x) =>
          x.asString exists {
            case "positions" => env.positions.exists(_.symbol == r.symbol)
            case "orders" => env.orders.exists(_.symbol == r.symbol)
            case s => console.error(s"in: Invalid collection '%s'", s); false
          }
        case x => console.error(s"symbol in [...]: Invalid operand '%j'", x); false
      }
    }
  }

}

/**
  * Rule Compiler Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object RuleCompiler {

  /**
    * Rule Compiler Extensions
    * @param jsValue the given [[js.Any value]]
    */
  implicit class RuleCompilerExtensions(val jsValue: js.Any) extends AnyVal {

    def as[T]: js.UndefOr[T] = jsValue.asInstanceOf[js.UndefOr[T]]

    def asString(implicit util: Util): js.UndefOr[String] = if (util.isString(jsValue)) jsValue.asInstanceOf[String] else js.undefined

    def is[T](value: T) = jsValue.asInstanceOf[js.UndefOr[T]].exists(_ == value)

  }

}