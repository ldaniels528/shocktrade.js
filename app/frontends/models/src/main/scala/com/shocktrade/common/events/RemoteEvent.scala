package com.shocktrade.common.events

import com.shocktrade.common.models.quote.Ticker
import io.scalajs.JSON

import scala.scalajs.js

/**
 * Represents a Remote Event
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait RemoteEvent extends js.Object {

  def action: js.UndefOr[String]

  def data: js.UndefOr[String]

}

/**
 * Remote Event Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object RemoteEvent {

  /**
   * Creates a new Remote Event
   * @param action the given action
   * @param data   the given data
   * @return a new [[RemoteEvent Remote Event]]
   */
  def apply(action: js.UndefOr[String], data: js.UndefOr[String]) = new DefaultEvent(action, data)

  def unapply(event: RemoteEvent): Option[(String, String)] = for {
    action <- event.action.toOption
    data <- event.data.toOption
  } yield (action, data)

  def createStockUpdateEvent(tickers: js.Array[Ticker]): RemoteEvent = {
    RemoteEvent(action = StockUpdateEvent, data = JSON.stringify(new StockUpdateEvent(tickers)))
  }

  def createUserStatusUpdateEvent(userID: js.UndefOr[String], connected: js.UndefOr[Boolean]): RemoteEvent = {
    RemoteEvent(action = UserStatusUpdateEvent, data = JSON.stringify(new UserStatusUpdateEvent(userID, connected)))
  }

  class DefaultEvent(val action: js.UndefOr[String], val data: js.UndefOr[String]) extends RemoteEvent

  class StockUpdateEvent(val tickers: js.Array[Ticker]) extends js.Object

  class UserStatusUpdateEvent(val userID: js.UndefOr[String], val connected: js.UndefOr[Boolean]) extends js.Object

  /////////////////////////////////////////////////////////////////////
  //          Contest Events
  /////////////////////////////////////////////////////////////////////

  val ContestCreated = "contest_created"
  val ContestDeleted = "contest_deleted"
  val ContestSelected = "contest_selected"

  /////////////////////////////////////////////////////////////////////
  //          Contest-Participant Events
  /////////////////////////////////////////////////////////////////////

  val ChatMessagesUpdated = "chat_messages_updated"
  val OrderUpdated = "orders_updated"
  val PortfolioUpdated = "portfolio_updated"
  val PerksUpdated = "perks_updated"

  /////////////////////////////////////////////////////////////////////
  //          User Profile Events
  /////////////////////////////////////////////////////////////////////

  val AwardsUpdated = "awards_updated"
  val UserProfileUpdated = "user_profile_updated"
  val UserStatusUpdateEvent = "UserStatusUpdateEvent"

  /////////////////////////////////////////////////////////////////////
  //          Stock Events
  /////////////////////////////////////////////////////////////////////

  val StockUpdateEvent = "StockUpdateEvent"

}
