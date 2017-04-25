package com.shocktrade.server.dao.processes

import io.scalajs.npm.mongodb.ObjectID
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
  * Batch Process Data
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
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