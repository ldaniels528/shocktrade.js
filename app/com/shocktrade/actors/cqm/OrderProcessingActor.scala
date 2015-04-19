package com.shocktrade.actors.cqm

import akka.actor.{Actor, ActorLogging}
import com.shocktrade.controllers.QuoteResources._
import play.modules.reactivemongo.json.collection.JSONCollection

/**
 * Order Processing Actor
 * @author lawrence.daniels@gmail.com
 */
class OrderProcessingActor() extends Actor with ActorLogging {
  lazy val mcC = db.collection[JSONCollection]("Contests")

  override def receive = {
    case message =>
      unhandled(message)
  }
}
