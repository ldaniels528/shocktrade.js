package com.shocktrade.common.events

import scala.scalajs.js

/**
 * Represents a Remote Event
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RemoteEvent(val action: js.UndefOr[String], val data: js.UndefOr[String]) extends js.Object

/**
 * Remote Event Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object RemoteEvent {

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
  val NetWorthUpdated = "net_worth_updated"
  val UserProfileUpdated = "user_profile_updated"

}
