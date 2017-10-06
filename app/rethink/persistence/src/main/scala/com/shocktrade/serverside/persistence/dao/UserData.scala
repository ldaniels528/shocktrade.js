package com.shocktrade.serverside.persistence.dao

import scala.scalajs.js

/**
  * Represents a user data object
  * @author lawrence.daniels@gmail.com
  */
class UserData(val userID: js.UndefOr[String],
               val name: js.UndefOr[String],
               val funds: js.UndefOr[Double],
               val creationTime: js.UndefOr[js.Date]) extends js.Object