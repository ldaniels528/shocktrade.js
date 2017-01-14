package com.shocktrade.autonomous

import com.shocktrade.autonomous.RuleProcessor._
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.server.common.LoggerFactory

import scala.language.postfixOps
import scala.scalajs.js

/**
  * Rule Processor
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RuleProcessor() {

  /**
    * Executes the compiled strategy
    * @param opCodes    the opCodes representing the compiled strategy
    * @param securities the securities to evaluate
    * @param env        the robot environment
    * @return the filtered set of securities
    */
  def apply(opCodes: Seq[OpCode], securities: Seq[ResearchQuote])(implicit env: RobotEnvironment) = {
    opCodes.foldLeft(securities) { (inputSet, opCode) =>
      val outputSet = inputSet.filterNot(opCode.filter)
      opCode.log(s"(in: ${inputSet.size} => out: ${outputSet.size})")
      outputSet
    }
  }

}

/**
  * Rule Processor Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object RuleProcessor {
  private[this] val logger = LoggerFactory.getLogger(getClass)

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
    def log(format: String, args: Any*)(implicit env: RobotEnvironment) = {
      logger.log(s"[${env.name}] ${opCode.name} $format", args: _*)
    }
  }

}