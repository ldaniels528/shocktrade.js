package com.shocktrade.webapp.vm.dao

case class PortfolioClosedException(portfolioID: String) extends RuntimeException {
  override def fillInStackTrace(): Throwable = {
    scala.scalajs.runtime.StackTrace.captureState(this, s"Portfolio $portfolioID is closed")
    this
  }
}
