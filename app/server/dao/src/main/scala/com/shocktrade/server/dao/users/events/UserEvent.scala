package com.shocktrade.server.dao.users.events

import com.shocktrade.server.dao.events.SourcedEvent

/**
  * Base class for user events
  * @author lawrence.daniels@gmail.com
  */
sealed trait UserEvent extends SourcedEvent
