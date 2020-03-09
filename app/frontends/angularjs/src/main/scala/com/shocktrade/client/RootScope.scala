package com.shocktrade.client

import com.shocktrade.client.models.UserProfile
import com.shocktrade.common.models.user.NetWorth
import io.scalajs.npm.angularjs.Scope

import scala.scalajs.js

/**
 * Represents the application $rootScope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait RootScope extends Scope {
  var userProfile: js.UndefOr[UserProfile] = js.undefined
  var netWorth: js.UndefOr[NetWorth] = js.undefined

}
