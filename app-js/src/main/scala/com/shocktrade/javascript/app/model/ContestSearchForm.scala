package com.shocktrade.javascript.app.model

/**
 * {"activeOnly":true,"available":false,"perksAllowed":false,"levelCap":"1","levelCapAllowed":true,"friendsOnly":true,"restrictionUsed":true}
 */
case class ContestSearchForm(activeOnly: Option[Boolean],
                             available: Option[Boolean],
                             friendsOnly: Option[Boolean],
                             invitationOnly: Option[Boolean],
                             levelCap: Option[String],
                             levelCapAllowed: Option[Boolean],
                             perksAllowed: Option[Boolean],
                             robotsAllowed: Option[Boolean])

