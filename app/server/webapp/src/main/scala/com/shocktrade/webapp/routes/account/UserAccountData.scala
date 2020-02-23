package com.shocktrade.webapp.routes.account

import scala.scalajs.js

/**
 * Represents a user account record
 */
class UserAccountData(val userID: js.UndefOr[String] = js.undefined,
                      val username: js.UndefOr[String],
                      val email: js.UndefOr[String],
                      val password: js.UndefOr[String],
                      val wallet: js.UndefOr[Double],
                      val creationTime: js.UndefOr[js.Date] = js.undefined,
                      val lastModifiedTime: js.UndefOr[js.Date] = js.undefined) extends js.Object