package com.shocktrade.common.auth

import scala.scalajs.js

/**
 * Authentication Response
 * @param userID the authenticated user ID
 */
class AuthenticationResponse(val userID: js.UndefOr[String] = js.undefined,
                             val username: js.UndefOr[String],
                             val email: js.UndefOr[String],
                             val wallet: js.UndefOr[Double]) extends js.Object
