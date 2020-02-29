package com.shocktrade.client.contest

import scala.scalajs.js

/**
  * Game Level
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class GameLevel(val number: Int, val nextLevelXP: Int, val description: String) extends js.Object

/**
  * Game Level Singleton
  */
object GameLevel {

  val Levels: js.Array[GameLevel] = js.Array(
    new GameLevel(number = 1, nextLevelXP = 1000, description = "Private"),
    new GameLevel(number = 2, nextLevelXP = 2000, description = "Private 1st Class"),
    new GameLevel(number = 3, nextLevelXP = 4000, description = "Corporal"),
    new GameLevel(number = 4, nextLevelXP = 8000, description = "First Corporal"),
    new GameLevel(number = 5, nextLevelXP = 16000, description = "Sergeant"),
    new GameLevel(number = 6, nextLevelXP = 32000, description = "Staff Sergeant"),
    new GameLevel(number = 7, nextLevelXP = 64000, description = "Gunnery Sergeant"),
    new GameLevel(number = 8, nextLevelXP = 1280000, description = "Master Sergeant"),
    new GameLevel(number = 9, nextLevelXP = 256000, description = "First Sergeant"),
    new GameLevel(number = 10, nextLevelXP = 1024000, description = "Sergeant Major"),
    new GameLevel(number = 11, nextLevelXP = 2048000, description = "Warrant Officer 3rd Class"),
    new GameLevel(number = 12, nextLevelXP = 4096000, description = "Warrant Officer 2nd Class"),
    new GameLevel(number = 13, nextLevelXP = 4096000, description = "Warrant Officer 1st Class"),
    new GameLevel(number = 14, nextLevelXP = 8192000, description = "Chief Warrant Officer"),
    new GameLevel(number = 15, nextLevelXP = 8192000, description = "Master Chief Warrant Officer"),
    new GameLevel(number = 16, nextLevelXP = 16384000, description = "Lieutenant"),
    new GameLevel(number = 17, nextLevelXP = 32768000, description = "First Lieutenant"),
    new GameLevel(number = 18, nextLevelXP = 65536000, description = "Captain"),
    new GameLevel(number = 19, nextLevelXP = 131072000, description = "Major"),
    new GameLevel(number = 20, nextLevelXP = 262144000, description = "Lieutenant Colonel"),
    new GameLevel(number = 21, nextLevelXP = 524288000, description = "Colonel"),
    new GameLevel(number = 22, nextLevelXP = 524288000, description = "Brigadier General"),
    new GameLevel(number = 23, nextLevelXP = 524288000, description = "Major General"),
    new GameLevel(number = 24, nextLevelXP = 524288000, description = "Lieutenant General"),
    new GameLevel(number = 25, nextLevelXP = 524288000, description = "General"))

}