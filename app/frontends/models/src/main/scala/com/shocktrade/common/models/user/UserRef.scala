package com.shocktrade.common.models.user

import scala.scalajs.js

/**
 * Represents a reference to a user
 * @param userID   the given user ID
 * @param username the given username
 */
class UserRef(val userID: js.UndefOr[String], val username: js.UndefOr[String]) extends js.Object

/**
 * User Reference Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object UserRef {

  def apply(userID: js.UndefOr[String], username: js.UndefOr[String]): UserRef = new UserRef(userID, username)

  def unapply(ref: UserRef): Option[(String, String)] = for {
    userID <- ref.userID.toOption
    username <- ref.username.toOption
  } yield (userID, username)

}