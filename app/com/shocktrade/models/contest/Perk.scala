package com.shocktrade.models.contest

import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Represents a Perk
 * @author lawrence.daniels@gmail.com
 */
case class Perk(name: String,
                code: String,
                description: String,
                cost: Double)

/**
 * Perk Singleton
 * @author lawrence.daniels@gmail.com
 */
object Perk {

  implicit val perkReads: Reads[Perk] = (
    (__ \ "name").read[String] and
      (__ \ "code").read[String] and
      (__ \ "description").read[String] and
      (__ \ "cost").read[Double])(Perk.apply _)

  implicit val perkWrites: Writes[Perk] = (
    (__ \ "name").write[String] and
      (__ \ "code").write[String] and
      (__ \ "description").write[String] and
      (__ \ "cost").write[Double])(unlift(Perk.unapply))

}