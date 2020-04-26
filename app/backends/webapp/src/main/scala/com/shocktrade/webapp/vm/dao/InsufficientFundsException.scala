package com.shocktrade.webapp.vm.dao

case class InsufficientFundsException(actual: Double, expected: Double) extends RuntimeException {
  val message = f"Insufficient funds (have: $actual%.5f, need: $expected%.5f)"
  override def fillInStackTrace(): Throwable = {
    scala.scalajs.runtime.StackTrace.captureState(this, message)
    this
  }
}
