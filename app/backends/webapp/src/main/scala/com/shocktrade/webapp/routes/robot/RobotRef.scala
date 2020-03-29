package com.shocktrade.webapp.routes.robot

import com.shocktrade.webapp.routes.robot.dao.RobotData
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Represents a reference to a robot user
 * @param userID    the given user ID
 * @param username  the given username
 * @param contestID the given contestID
 */
class RobotRef(val userID: js.UndefOr[String],
               val username: js.UndefOr[String],
               val contestID: js.UndefOr[String],
               val portfolioID: js.UndefOr[String]) extends js.Object

/**
 * Robot Reference Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object RobotRef {

  def apply(userID: js.UndefOr[String],
            username: js.UndefOr[String],
            contestID: js.UndefOr[String],
            portfolioID: js.UndefOr[String]): RobotRef = new RobotRef(userID, username, contestID, portfolioID)

  def apply(data: RobotData): RobotRef = new RobotRef(data.userID, data.username, data.contestID, data.portfolioID)

  def unapply(ref: RobotRef): Option[(String, String, String, String)] = (for {
    userID <- ref.userID
    username <- ref.username
    contestID <- ref.contestID
    portfolioID <- ref.portfolioID
  } yield (userID, username, contestID, portfolioID)).toOption

  final implicit class RobotRefEnriched(val robotRef: RobotRef) extends AnyVal {
    def copy(userID: js.UndefOr[String] = js.undefined,
             username: js.UndefOr[String] = js.undefined,
             contestID: js.UndefOr[String] = js.undefined,
             portfolioID: js.UndefOr[String] = js.undefined): RobotRef = {
      RobotRef(
        userID = userID ?? robotRef.userID,
        username = username ?? robotRef.username,
        contestID = contestID ?? robotRef.contestID,
        portfolioID = portfolioID ?? robotRef.portfolioID)
    }
  }

}