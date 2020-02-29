package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
  * Contest Portfolio Rankings
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ContestRankings(var participants: js.UndefOr[js.Array[Participant]] = js.undefined,
                      var leader: js.UndefOr[Participant] = js.undefined,
                      var player: js.UndefOr[Participant] = js.undefined) extends js.Object

