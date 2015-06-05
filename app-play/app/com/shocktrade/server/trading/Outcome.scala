package com.shocktrade.server.trading

import reactivemongo.core.commands.LastError

/**
 * Represents the Outcome of a Trading Operation
 * @author lawrence.daniels@gmail.com
 */
trait Outcome {

  def isSuccess: Boolean = false

  def isFailure: Boolean = false

}

/**
 * Outcome Singleton
 * @author lawrence.daniels@gmail.com
 */
object Outcome {

  def apply(result: LastError): Outcome = {
    if(result.inError) Failed(result.message) else Succeeded(result.n)
  }

  /**
   * Represents a successful outome
   * @param count the number of records updated
   */
  case class Succeeded(count: Int) extends Outcome {

    override def isSuccess: Boolean = true

  }

  /**
   * Represents a failed outome
   * @param message the given error message
   * @param exc the option of an [[Throwable exception]]
   */
  case class Failed(message: String, exc: Option[Throwable] = None) extends Outcome {

    override def isFailure: Boolean = true

  }

}
