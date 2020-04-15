package com.shocktrade.remote.loader

import com.shocktrade.common.models.OperationResult
import com.shocktrade.common.util.StringHelper._
import com.shocktrade.common.util.TupleHelper._
import com.shocktrade.remote.loader.Command.Implicits._
import com.shocktrade.remote.proxies.{ContestProxy, PortfolioProxy, UserProxy}
import io.scalajs.JSON

import scala.concurrent.Future
import scala.scalajs.js

/**
 * Class representation of a command
 * @param name          the name of the command
 * @param args          the command line arguments
 * @param correlationID the optional callback ID
 */
class Command(val name: String, val args: List[String], val correlationID: Option[String]) {

  def invoke()(implicit c: ContestProxy, p: PortfolioProxy, u: UserProxy): Future[js.Any] = {
    name match {
      case "cancelOrder" => args.twoIds into p.cancelOrder
      case "contestSearch" => c.contestSearch(args.oneJSON)
      case "createNewGame" => c.createNewGame(args.oneJSON)
      case "createOrder" => args.twoIdsAndJSON into p.createOrder
      case "findContestByID" => c.findContestByID(args.oneId)
      case "findContestRankings" => c.findContestRankings(args.oneId)
      case "findOrders" => args.twoIds into p.findOrders
      case "findPortfolioBalance" => args.twoIds into p.findPortfolioBalance
      case "findPortfolioByID" => p.findPortfolioByID(args.oneId)
      case "findPositions" => args.twoIds into p.findPositions
      case "findUserByID" => u.findUserByID(args.oneId)
      case "findUserByName" => u.findUserByName(args.oneId)
      case "joinContest" => args.twoIds into c.joinContest
      case "putChatMessage" => args.idAndJSON into c.putChatMessage
      case "print" => printCompact(message = args.oneId)
      case "slice" => args.twoIdsAndJSON into slice
      case unknown => throw js.JavaScriptException(s"Command '$unknown' not recognized")
    }
  }

  private def printCompact(message: String): Future[OperationResult] = Future.successful {
    val displayMessage = if (message.startsWith("\"") && message.endsWith("\"")) message.drop(1).dropRight(1) else message
    println(displayMessage)
    new OperationResult(success = true)
  }

  private def slice(start: String, end: String, instance: js.Any): Future[js.Any] = Future.successful {
    val _start = start.toInt
    val _end = end.toInt
    instance match {
      case obj if js.Array.isArray(obj) => obj.asInstanceOf[js.Array[js.Any]].slice(_start, _end)
      case obj => obj
    }
  }

}

/**
 * Command Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Command {

  def apply(name: String, args: List[String], correlationID: Option[String]) = new Command(name, args, correlationID)

  def parseCommand(line: String): Command = {
    val chunks = line.safeSplit(delimiter = ' ', limit = 4)
    chunks match {
      case correlationID :: name :: args if correlationID.startsWith("[") & correlationID.endsWith("]") =>
        Command(name, args, Some(correlationID.drop(1).dropRight(1)))
      case name :: args =>
        Command(name, args, correlationID = None)
      case _ => throw js.JavaScriptException(s"Syntax error in '$line'")
    }
  }

  object Implicits {

    /**
     * Arguments Enriched
     * @param args the given arguments
     */
    final implicit class ArgumentsEnriched(val args: Seq[String]) extends AnyVal {

      def idAndJSON[A <: js.Any]: (String, A) = args.toList match {
        case id :: arg :: Nil => id -> JSON.parseAs[A](arg)
        case _ => throw js.JavaScriptException("An identifier and a JSON argument was expected")
      }

      def oneId: String = args.toList match {
        case arg :: Nil => arg
        case _ => throw js.JavaScriptException("A single String argument was expected")
      }

      def oneJSON[A <: js.Any]: A = args.toList match {
        case arg :: Nil => JSON.parseAs[A](arg)
        case _ => throw js.JavaScriptException("A single JSON argument was expected")
      }

      def twoIds: (String, String) = args.toList match {
        case id0 :: id1 :: Nil => (id0, id1)
        case _ => throw js.JavaScriptException("Two identifier arguments were expected")
      }

      def twoIdsAndJSON[A <: js.Any]: (String, String, A) = args.toList match {
        case id0 :: id1 :: arg :: Nil => (id0, id1, JSON.parseAs[A](arg))
        case _ => throw js.JavaScriptException("Two identifiers and a JSON argument were expected")
      }

    }

  }

}
