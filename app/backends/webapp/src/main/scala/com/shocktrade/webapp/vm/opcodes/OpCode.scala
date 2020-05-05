package com.shocktrade.webapp.vm
package opcodes

import io.scalajs.JSON

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Represents Contest Virtual Machine (CVM) Operational code
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait OpCode {

  def invoke()(implicit ctx: VirtualMachineContext, ec: ExecutionContext): Future[Any]

  def toJsObject: EventSourceIndex = EventSourceIndex(
    "type" -> getClass.getSimpleName
  )

  override def toString: String = {
    val index = toJsObject
    (for {
      name <- index.`type`
      filteredProps = js.Dictionary(index.asInstanceOf[js.Dictionary[_]].toSeq.filterNot(_._1 == "type"): _*)
    } yield s"$name(${JSON.stringify(filteredProps)})") getOrElse JSON.stringify(index)
  }

}

/**
 * Represents the event source index columns
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait EventSourceIndex extends js.Object {

  def `type`: js.UndefOr[String]

  def contestID: js.UndefOr[String]

  def portfolioID: js.UndefOr[String]

  def positionID: js.UndefOr[String]

  def userID: js.UndefOr[String]

  def orderID: js.UndefOr[String]

  def symbol: js.UndefOr[String]

  def exchange: js.UndefOr[String]

}

/**
 * Event Source Index Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object EventSourceIndex {

  def apply(props: (String, js.Any)*): EventSourceIndex = {
    js.Dictionary(props: _*).asInstanceOf[EventSourceIndex]
  }

  final implicit class EventSourceIndexEnriched(val index: EventSourceIndex) extends AnyVal {

    @inline
    def ++(anotherIndex: EventSourceIndex): EventSourceIndex = {
      EventSourceIndex((index.toProperties ++ anotherIndex.toProperties).toSeq: _*)
    }

   @inline
   def toProperties: js.Dictionary[js.Any] = index.asInstanceOf[js.Dictionary[js.Any]]

  }

}
