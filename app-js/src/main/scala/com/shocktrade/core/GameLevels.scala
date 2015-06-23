package com.shocktrade.core

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{ literal => JS }

/**
 * Represents the Game Levels
 * @author lawrence.daniels@gmail.com
 */
object GameLevels {

  val Levels = js.Array(
    JS(number = 1, nextLevelXP = 1000, description = "Private"),
    JS(number = 2, nextLevelXP = 2000, description = "Private 1st Class"),
    JS(number = 3, nextLevelXP = 4000, description = "Corporal"),
    JS(number = 4, nextLevelXP = 8000, description = "First Corporal"),
    JS(number = 5, nextLevelXP = 16000, description = "Sergeant"),
    JS(number = 6, nextLevelXP = 32000, description = "Staff Sergeant"),
    JS(number = 7, nextLevelXP = 64000, description = "Gunnery Sergeant"),
    JS(number = 8, nextLevelXP = 1280000, description = "Master Sergeant"),
    JS(number = 9, nextLevelXP = 256000, description = "First Sergeant"),
    JS(number = 10, nextLevelXP = 1024000, description = "Sergeant Major"),
    JS(number = 11, nextLevelXP = 2048000, description = "Warrant Officer 3rd Class"),
    JS(number = 12, nextLevelXP = 4096000, description = "Warrant Officer 2nd Class"),
    JS(number = 13, nextLevelXP = 4096000, description = "Warrant Officer 1st Class"),
    JS(number = 14, nextLevelXP = 8192000, description = "Chief Warrant Officer"),
    JS(number = 15, nextLevelXP = 8192000, description = "Master Chief Warrant Officer"),
    JS(number = 16, nextLevelXP = 16384000, description = "Lieutenant"),
    JS(number = 17, nextLevelXP = 32768000, description = "First Lieutenant"),
    JS(number = 18, nextLevelXP = 65536000, description = "Captain"),
    JS(number = 19, nextLevelXP = 131072000, description = "Major"),
    JS(number = 20, nextLevelXP = 262144000, description = "Lieutenant Colonel"),
    JS(number = 21, nextLevelXP = 524288000, description = "Colonel"),
    JS(number = 22, nextLevelXP = 524288000, description = "Brigadier General"),
    JS(number = 23, nextLevelXP = 524288000, description = "Major General"),
    JS(number = 24, nextLevelXP = 524288000, description = "Lieutenant General"),
    JS(number = 25, nextLevelXP = 524288000, description = "General"))

}
