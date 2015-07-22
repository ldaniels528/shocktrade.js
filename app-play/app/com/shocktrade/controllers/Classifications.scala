package com.shocktrade.controllers

import com.shocktrade.controllers.QuotesController._
import com.shocktrade.models.quote.{NaicsCode, SicCode}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.{obj => JS}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, _}

import scala.concurrent.Future

/**
 * Classifications
 */
trait Classifications {
  lazy val mcN = db.collection[BSONCollection]("NAICS")
  lazy val mcS = db.collection[BSONCollection]("SIC")

  lazy val naicsCodes = loadNaicsMappings()
  lazy val sicCodes = loadSicsMappings()

  /**
   * Loads the NAICS codes mapping
   */
  private def loadNaicsMappings(): Future[Map[Int, String]] = {
    mcN.find(BS()).cursor[NaicsCode]().collect[Seq]() map {
      _ map { naicsCode => (naicsCode.naicsNumber, naicsCode.description) }
    } map (f => Map(f: _*))
  }

  /**
   * Loads the SIC codes mapping
   */
  private def loadSicsMappings(): Future[Map[Int, String]] = {
    mcS.find(BS()).cursor[SicCode]().collect[Seq]() map {
      _ map { sicCode => (sicCode.sicNumber, sicCode.description) }
    } map (f => Map(f: _*))
  }

}
