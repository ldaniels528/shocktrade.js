package com.shocktrade.javascript

/**
 * ShockTrade Application Events
 * @author lawrence.daniels@gmail.com
 */
object AppEvents {

  /////////////////////////////////////////////////////////////////////
  //          Contest Events
  /////////////////////////////////////////////////////////////////////

  val ContestCreated = "contest_created"

  val ContestDeleted = "contest_deleted"

  val ContestSelected = "contest_selected"

  val ContestUpdated = "contest_updated"

  val MessagesUpdated = "messages_updated"

  /////////////////////////////////////////////////////////////////////
  //          Contest-Participant Events
  /////////////////////////////////////////////////////////////////////

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
