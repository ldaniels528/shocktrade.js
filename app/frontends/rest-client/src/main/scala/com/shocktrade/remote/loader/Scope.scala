package com.shocktrade.remote.loader

import com.shocktrade.common.util.StringHelper._
import io.scalajs.JSON

import scala.scalajs.js

/**
 * Represents a scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Scope {
  private val tasks = js.Dictionary[Task]()

  var isDebug: Boolean = false

  def add(correlationID: String, task: Task): Unit = tasks(correlationID) = task

  def getTask(correlationID: String): Task = tasks.getOrElse(correlationID, die(s"Task $correlationID not found"))

}

/**
 * Scope Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Scope {
  private val anchor = "$$"

  def findVariables(line: String): List[VariableRef] = {
    var lastIndex = 0
    var list: List[VariableRef] = Nil
    do {
      val result = for {
        start <- line.indexOfOpt("$$", lastIndex)
        (name, end) <- findIdentifier(line, start)
      } yield VariableRef(name, start, end)

      result match {
        case Some(ref) =>
          lastIndex = ref.end
          list = ref :: list
        case None =>
          lastIndex = line.length
      }

    } while (lastIndex < line.length && line.indexOf("$$", lastIndex) >= 0)

    list.sortBy(-_.start)
  }

  private def findIdentifier(line: String, start: Int): Option[(String, Int)] = {
    val ca = line.toCharArray
    var end = start + anchor.length
    while (end < ca.length && (ca(end).isLetterOrDigit || ca(end) == '_' || ca(end) == '.')) end += 1
    val ref = line.substring(start + anchor.length, end)
    Option((ref, end))
  }

  /**
   * Represents a variable reference
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  case class VariableRef(name: String, start: Int, end: Int) {

    def getInstanceKey: String = name.indexOfOpt(".").map(name.substring(0, _)).getOrElse(name)

    def getReplacementValue(values: js.Dictionary[js.Any]): Any = {
      name.indexOfOpt(".") match {
        // instance and field? (e.g. $$userObject.userID)
        case Some(index) =>
          val (instanceKey, fieldKey) = (name.substring(0, index), name.substring(index + 1))
          val instance = values.getOrElse(instanceKey, die(s"Task result '$instanceKey' not found in ${JSON.stringify(values)}"))
          val instanceDict = instance.asInstanceOf[js.Dictionary[js.Any]]
          instanceDict.getOrElse(fieldKey, die(s"Task result field '$fieldKey' not found in ${JSON.stringify(instanceDict)}"))

        // instance only (e.g. $$userObject)
        case None =>
         values.getOrElse(name, die(s"Task result '$name' not found in ${JSON.stringify(values)}"))
      }
    }

    def replaceTags(line: String, values: js.Dictionary[js.Any]): String = {
      //values foreach { case (key, value) => if(key == "cc") println(s"$key => ${JSON.stringify( value, null, 4)}")}
      val replacementValue: String = getReplacementValue(values) match {
        case null => ""
        case s: String => s
        case x if x.toString.startsWith("[object") => JSON.stringify(x.asInstanceOf[js.Any])
        case x => x.toString
      }
      new StringBuilder(line).replace(start, end, replacementValue).toString()
    }

  }

}
