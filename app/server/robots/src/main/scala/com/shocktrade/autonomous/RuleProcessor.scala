package com.shocktrade.autonomous

import com.shocktrade.autonomous.RuleProcessor._
import com.shocktrade.common.models.quote.ResearchQuote
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.{NodeRequire, console}

import scala.language.postfixOps
import scala.scalajs.js

/**
  * Rule Processor
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RuleProcessor()(implicit require: NodeRequire) {
  private implicit val moment = Moment()

  /**
    * Executes the compiled strategy
    * @param opCodes    the opCodes representing the compiled strategy
    * @param securities the securities to evaluate
    * @param env        the robot environment
    * @return the filtered set of securities
    */
  def apply(opCodes: Seq[OpCode], securities: Seq[ResearchQuote])(implicit env: RobotEnvironment) = {
    opCodes.foldLeft(securities) { (inputSet, condition) =>
      val outputSet = inputSet.filterNot(condition.filter)
      condition.log(s"(in: ${inputSet.size} => out: ${outputSet.size})")
      outputSet
    }
  }

}

/**
  * Rule Processor Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object RuleProcessor {

  /**
    * IN [ ... ] op-code
    */
  @js.native
  trait InOp extends js.Object {
    def in: js.UndefOr[js.Array[js.Any]] = js.native
  }

  /**
    * OpCode Extensions
    * @param opCode the given [[OpCode OpCode]]
    */
  implicit class OpCodeExtensions(val opCode: OpCode) extends AnyVal {

    @inline
    def log(format: String, args: Any*)(implicit env: RobotEnvironment, moment: Moment) = {
      console.log(s"${moment().format("MM/DD HH:mm:ss")} [${env.name}] ${opCode.name} $format", args: _*)
    }
  }

}