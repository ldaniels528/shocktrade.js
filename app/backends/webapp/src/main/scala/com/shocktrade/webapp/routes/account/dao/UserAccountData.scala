package com.shocktrade.webapp.routes.account.dao

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Represents a user account record
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserAccountData(val userID: js.UndefOr[String] = js.undefined,
                      val username: js.UndefOr[String],
                      val email: js.UndefOr[String],
                      val password: js.UndefOr[String],
                      val wallet: js.UndefOr[Double],
                      val creationTime: js.UndefOr[js.Date] = js.undefined,
                      val lastModifiedTime: js.UndefOr[js.Date] = js.undefined) extends js.Object

/**
 * User Account Data Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object UserAccountData {

  /**
   * User Account Data Enrichment
   * @param accountData the given [[UserAccountData]]
   */
  final implicit class UserAccountDataEnrich(val accountData: UserAccountData) extends AnyVal {

    def copy(userID: js.UndefOr[String] = js.undefined,
             username: js.UndefOr[String] = js.undefined,
             email: js.UndefOr[String] = js.undefined,
             password: js.UndefOr[String] = js.undefined,
             wallet: js.UndefOr[Double] = js.undefined,
             creationTime: js.UndefOr[js.Date] = js.undefined,
             lastModifiedTime: js.UndefOr[js.Date] = js.undefined): UserAccountData = {
      new UserAccountData(
        userID = userID ?? accountData.userID,
        username = username ?? accountData.username,
        email = email ?? accountData.email,
        password = js.undefined, // passwords are never copied
        wallet = wallet ?? accountData.wallet,
        creationTime = creationTime ?? accountData.creationTime,
        lastModifiedTime = lastModifiedTime ?? accountData.lastModifiedTime)
    }
  }

}