package com.shocktrade.webapp.vm.dao

case class ContestPortfolioNotFoundException(contestID: String, userID: String) extends RuntimeException {
  override def fillInStackTrace(): Throwable = {
    scala.scalajs.runtime.StackTrace.captureState(this, s"Portfolio was not found [contest $contestID, user $userID ]")
    this
  }
}
