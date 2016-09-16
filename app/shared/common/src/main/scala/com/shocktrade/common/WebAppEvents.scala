package com.shocktrade.common

/**
  * Shocktrade Application Events
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object WebAppEvents {

  /////////////////////////////////////////////////////////////////////
  //          Contest Events
  /////////////////////////////////////////////////////////////////////

  val ContestCreated = "contest_created"
  val ContestDeleted = "contest_deleted"
  val ContestSelected = "contest_selected"
  val ContestUpdated = "contest_updated"

  /////////////////////////////////////////////////////////////////////
  //          Contest-Participant Events
  /////////////////////////////////////////////////////////////////////

  val ChatMessagesUpdated = "chat_messages_updated"
  val OrderUpdated = "orders_updated"
  val ParticipantUpdated = "participant_updated"
  val PerksUpdated = "perks_updated"

  /////////////////////////////////////////////////////////////////////
  //          User Profile Events
  /////////////////////////////////////////////////////////////////////

  val AwardsUpdated = "awards_updated"
  val UserProfileChanged = "user_profile_changed"
  val UserProfileUpdated = "profile_updated"
  val UserStatusChanged = "user_status_changed"

}
