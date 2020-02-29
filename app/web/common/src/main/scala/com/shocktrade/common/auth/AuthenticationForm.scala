package com.shocktrade.common.auth

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Authentication Form
 * @param username the given user name
 */
class AuthenticationForm(val username: js.UndefOr[String] = js.undefined,
                         val password: js.UndefOr[String] = js.undefined,
                         val authCode: js.UndefOr[String] = js.undefined) extends js.Object

/**
 * Authentication Form Companion
 */
object AuthenticationForm {

  final implicit class AuthenticationFormExtensions(val form: AuthenticationForm) extends AnyVal {

    def copy(username: js.UndefOr[String] = js.undefined,
             password: js.UndefOr[String] = js.undefined,
             authCode: js.UndefOr[String] = js.undefined): AuthenticationForm = {
      new AuthenticationForm(
        username = username ?? form.username,
        password = password ?? form.password,
        authCode = authCode ?? form.authCode
      )
    }
  }

}