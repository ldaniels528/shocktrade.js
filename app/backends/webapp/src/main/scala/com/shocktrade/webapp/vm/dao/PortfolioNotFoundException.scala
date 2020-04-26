package com.shocktrade.webapp.vm.dao

case class PortfolioNotFoundException(portfolioID: String) extends RuntimeException {
  val message = s"Portfolio $portfolioID was not found"
  override def fillInStackTrace(): Throwable = {
    scala.scalajs.runtime.StackTrace.captureState(this, message)
    this
  }
}
