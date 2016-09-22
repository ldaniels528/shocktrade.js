package com.shocktrade.common.dao.processes

import org.scalajs.nodejs.mongodb.ObjectID
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Batch Process Data
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class BatchProcessData(var _id: js.UndefOr[ObjectID] = js.undefined,
                       var name: js.UndefOr[String] = js.undefined,
                       var enabled: js.UndefOr[Boolean]) extends js.Object

/**
  * Batch Process Data Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object BatchProcessData {

  /**
    * Batch Process Data Enrichment
    * @param data the given [[BatchProcessData batch process]] data object
    */
  implicit class BatchProcessDataEnrichment(val data: BatchProcessData) extends AnyVal {

    @inline
    def isEnabled = data.enabled.isTrue

  }

}