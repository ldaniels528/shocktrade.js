package com.shocktrade.client.models.contest

import scala.scalajs.js

/**
  * Contest Search Options
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ContestSearchOptions(var activeOnly: Boolean = false,
                           var available: Boolean = false,
                           var friendsOnly: Boolean = false,
                           var levelCap: String = "1",
                           var levelCapAllowed: Boolean = false,
                           var perksAllowed: Boolean = false,
                           var robotsAllowed: Boolean = false) extends js.Object