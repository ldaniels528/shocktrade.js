package com.shocktrade.webapp.vm.dao

case class PortfolioNotFoundException(portfolioID: String) extends RuntimeException {
  override def fillInStackTrace(): Throwable = {
    scala.scalajs.runtime.StackTrace.captureState(this, s"Portfolio $portfolioID was not found")
    this
  }
}
