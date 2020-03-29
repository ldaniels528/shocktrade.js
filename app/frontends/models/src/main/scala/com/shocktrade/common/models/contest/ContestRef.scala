package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a reference to a contest
 * @param contestID the given user ID
 * @param name      the given contest name
 */
class ContestRef(val contestID: js.UndefOr[String], val name: js.UndefOr[String]) extends js.Object

/**
 * Contest Reference Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestRef {

  def apply(contestID: js.UndefOr[String], name: js.UndefOr[String]): ContestRef = new ContestRef(contestID, name)

  def unapply(ref: ContestRef): Option[(String, String)] = (for {
    contestID <- ref.contestID
    name <- ref.name
  } yield (contestID, name)).toOption

}