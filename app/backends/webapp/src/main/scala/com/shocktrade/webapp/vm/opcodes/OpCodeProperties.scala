package com.shocktrade.webapp.vm.opcodes

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Represents the event source index columns
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait OpCodeProperties extends js.Object {

  def `type`: js.UndefOr[String]

  def contestID: js.UndefOr[String]

  def portfolioID: js.UndefOr[String]

  def positionID: js.UndefOr[String]

  def userID: js.UndefOr[String]

  def orderID: js.UndefOr[String]

  def symbol: js.UndefOr[String]

  def exchange: js.UndefOr[String]

  def orderType: js.UndefOr[String]

  def priceType: js.UndefOr[String]

  def quantity: js.UndefOr[Double]

  def negotiatedPrice: js.UndefOr[Double]

  def xp: js.UndefOr[Double]

}

/**
 * OpCode Properties Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OpCodeProperties {

  def apply(props: (String, js.Any)*): OpCodeProperties = {
    js.Dictionary(props: _*).asInstanceOf[OpCodeProperties]
  }

  /**
   * OpCode Properties Enriched
   * @param props the host [[OpCodeProperties]]
   */
  final implicit class OpCodePropertiesEnriched(val props: OpCodeProperties) extends AnyVal {

    @inline
    def compile: js.UndefOr[OpCode] = OpCode.compile(props)

    @inline
    def getAs[T](key: String): js.UndefOr[T] = toProperties.get(key).orUndefined.map(_.asInstanceOf[T])

    @inline
    def ++(anotherIndex: OpCodeProperties): OpCodeProperties = {
      OpCodeProperties((props.toProperties ++ anotherIndex.toProperties).toSeq: _*)
    }

    @inline
    def toProperties: js.Dictionary[js.Any] = props.asInstanceOf[js.Dictionary[js.Any]]

  }

}
