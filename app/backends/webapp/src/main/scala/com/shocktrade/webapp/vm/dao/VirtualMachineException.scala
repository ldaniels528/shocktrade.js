package com.shocktrade.webapp.vm.dao

class VirtualMachineException(message: String) extends RuntimeException(message) {
  override def fillInStackTrace(): Throwable = {
    scala.scalajs.runtime.StackTrace.captureState(this, getMessage)
    this
  }
}
