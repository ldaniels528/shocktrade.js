package com.shocktrade.javascript

import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

import scala.scalajs.js

/**
 * Game Level
 */
trait GameLevel extends js.Object {
  var number: Int = js.native
  var nextLevelXP: Int = js.native
  var description: String = js.native
}

/**
 * Game Level Singleton
 */
object GameLevel {

  val Levels = js.Array(
    GameLevel(number = 1, nextLevelXP = 1000, description = "Private"),
    GameLevel(number = 2, nextLevelXP = 2000, description = "Private 1st Class"),
    GameLevel(number = 3, nextLevelXP = 4000, description = "Corporal"),
    GameLevel(number = 4, nextLevelXP = 8000, description = "First Corporal"),
    GameLevel(number = 5, nextLevelXP = 16000, description = "Sergeant"),
    GameLevel(number = 6, nextLevelXP = 32000, description = "Staff Sergeant"),
    GameLevel(number = 7, nextLevelXP = 64000, description = "Gunnery Sergeant"),
    GameLevel(number = 8, nextLevelXP = 1280000, description = "Master Sergeant"),
    GameLevel(number = 9, nextLevelXP = 256000, description = "First Sergeant"),
    GameLevel(number = 10, nextLevelXP = 1024000, description = "Sergeant Major"),
    GameLevel(number = 11, nextLevelXP = 2048000, description = "Warrant Officer 3rd Class"),
    GameLevel(number = 12, nextLevelXP = 4096000, description = "Warrant Officer 2nd Class"),
    GameLevel(number = 13, nextLevelXP = 4096000, description = "Warrant Officer 1st Class"),
    GameLevel(number = 14, nextLevelXP = 8192000, description = "Chief Warrant Officer"),
    GameLevel(number = 15, nextLevelXP = 8192000, description = "Master Chief Warrant Officer"),
    GameLevel(number = 16, nextLevelXP = 16384000, description = "Lieutenant"),
    GameLevel(number = 17, nextLevelXP = 32768000, description = "First Lieutenant"),
    GameLevel(number = 18, nextLevelXP = 65536000, description = "Captain"),
    GameLevel(number = 19, nextLevelXP = 131072000, description = "Major"),
    GameLevel(number = 20, nextLevelXP = 262144000, description = "Lieutenant Colonel"),
    GameLevel(number = 21, nextLevelXP = 524288000, description = "Colonel"),
    GameLevel(number = 22, nextLevelXP = 524288000, description = "Brigadier General"),
    GameLevel(number = 23, nextLevelXP = 524288000, description = "Major General"),
    GameLevel(number = 24, nextLevelXP = 524288000, description = "Lieutenant General"),
    GameLevel(number = 25, nextLevelXP = 524288000, description = "General"))

  def apply(number: Int, nextLevelXP: Int, description: String) = {
    val level = makeNew[GameLevel]
    level.number = number
    level.nextLevelXP = nextLevelXP
    level.description = description
    level
  }

}