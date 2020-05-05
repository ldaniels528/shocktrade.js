package com.shocktrade.remote.loader

import com.shocktrade.common.util.StringHelper._
import com.shocktrade.common.util.TupleHelper._
import com.shocktrade.remote.loader.Command.Implicits._
import com.shocktrade.remote.proxies.{ContestProxy, PortfolioProxy, UserProxy}
import com.shocktrade.server.common.LoggerFactory
import io.scalajs.JSON
import io.scalajs.npm.request.{Request, RequestOptions}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Class representation of a command
 * @param name          the name of the command
 * @param args          the command line arguments
 * @param correlationID the optional callback ID
 */
class Command(val name: String, val args: List[String], val lineNumber: Int, val correlationID: Option[String]) {
  private val logger = LoggerFactory.getLogger(getClass)

  def invoke()(implicit c: ContestProxy, p: PortfolioProxy, u: UserProxy, scope: Scope): Future[js.Any] = {
    name match {
      // REST API functions
      case "cancelOrder" => p.cancelOrder(args.oneId)
      case "contestSearch" => c.contestSearch(args.oneJSON)
      case "createContest" => c.createContest(args.oneJSON)
      case "createOrder" => args.twoIdsAndJSON map p.createOrder
      case "findContestByID" => c.findContestByID(args.oneId)
      case "findContestRankings" => c.findContestRankings(args.oneId)
      case "findOrders" => args.twoIds map p.findOrders
      case "findPortfolioBalance" => args.twoIds map p.findPortfolioBalance
      case "findPortfolioByID" => p.findPortfolioByID(args.oneId)
      case "findPositions" => args.twoIds map p.findPositions
      case "findUserByID" => u.findUserByID(args.oneId)
      case "findUserByName" => u.findUserByName(args.oneId)
      case "joinContest" => args.twoIds map c.joinContest
      case "sendChatMessage" => args.idAndJSON map c.sendChatMessage

      // builtin functions
      case "arrayContains" => arrayContains(args)
      case "arrayGet" => arrayGet(args)
      case "arraySet" => arraySet(args)
      case "arrayLength" => arrayLength(args.oneJSON)
      case "arraySlice" => arraySlice(args)
      case "debug" => debug(args.oneId)
      case "delete" => httpDelete(args)
      case "get" => httpGet(args.oneId)
      case "head" => httpHead(args.oneId)
      case "patch" => httpPatch(args.oneId)
      case "post" => httpPost(args)
      case "put" => httpPut(args)
      case "print" => printCompact(message = args.oneId)
      case unknown => throw js.JavaScriptException(s"Command '$unknown' not recognized")
    }
  }

  def error(message: String): Unit = logger.error(f"[$lineNumber%04d] $message")

  def info(message: String)(implicit scope: Scope): Unit = if (scope.isDebug) logger.info(f"[$lineNumber%04d] $message")

  /**
   * Indicates whether the given array contains the given value
   * @param args the given arguments
   * @return true, if the given array contains the given value; otherwise false
   * @example arrayContains "Hello" { "greetings": [ ... ] " }
   */
  private def arrayContains(args: List[String]): Future[js.Any] = Future {
    args match {
      case anArray :: aValue :: Nil =>
        val array = JSON.parseAs[js.Any](anArray).asInstanceOf[js.Array[Any]]
        array.contains(aValue)
      case _ => throw js.JavaScriptException("A single JSON argument was expected")
    }
  }

  /**
   * Retrieves the object at the given index within the given array
   * @param args the given arguments
   * @return the object at the given index
   * @example arrayGet 0 $$users
   */
  private def arrayGet(args: List[String]): Future[js.Any] = Future {
    args match {
      case anIndex :: anArray :: Nil =>
        val (array, index) = (JSON.parseAs[js.Array[js.Any]](anArray), anIndex.toInt)
        array.slice(index, index + 1).headOption.orUndefined
      case _ => throw js.JavaScriptException("Syntax error. Usage: arrayGet <index> <array>")
    }
  }

  private def arraySet(args: List[String]): Future[js.Any] = Future {
    args match {
      case anArray :: anIndex :: aValue :: Nil =>
        val array = JSON.parseAs[js.Array[Any]](anArray)
        val index = anIndex.toInt
        val value = if(aValue.startsWith("{") || aValue.startsWith("[")) JSON.parse(aValue) else aValue
        array(index) = value
      case _ => throw js.JavaScriptException("Syntax error. Usage: arraySet <array> <index> <value>")
    }
  }

  private def arrayLength(array: js.Any): Future[js.Any] = Future {
    array match {
      case obj if js.Array.isArray(obj) => obj.asInstanceOf[js.Array[js.Any]].length
      case obj => die(s"Object is not an array in ${JSON.stringify(obj)}")
    }
  }

  /**
   * Retrieves the sub-array from given array from between the start to the end
   * @param args the given arguments
   * @return the sub-array
   * @example slice $$users 0 3
   */
  private def arraySlice(args: List[String]): Future[js.Array[js.Any]] = Future {
    args match {
      case anArray :: aStart :: anEnd :: Nil =>
        val start = aStart.toInt
        val end = anEnd.toInt
        val obj = JSON.parse(anArray)
        if (!js.Array.isArray(obj)) throw js.JavaScriptException(s"'$anArray' is not an array")
        else obj.asInstanceOf[js.Array[js.Any]].slice(start, end)
      case _ => throw js.JavaScriptException("Two identifiers and a JSON argument were expected")
    }
  }

  private def httpDelete(args: List[String]): Future[js.Any] = {
    args match {
      case url :: body :: Nil if body.startsWith("{") => Request.deleteFuture[js.Any](new RequestOptions(url = url, body = JSON.parse(body), json = true)).map(_._2)
      case url :: body :: Nil => Request.deleteFuture[js.Any](new RequestOptions(url = url, body = body)).map(_._2)
      case url :: Nil => Request.deleteFuture[js.Any](url).map(_._2)
      case _ => throw js.JavaScriptException("Syntax error; usage: post <url> [<body>]")
    }
  }

  private def debug(state: String)(implicit scope: Scope): Future[js.Any] = Future {
    scope.isDebug = state match {
      case "false" | "no" | "off" => false
      case "true" | "yes" | "on" => true
      case value => throw js.JavaScriptException(s"Syntax error near '$value'; usage: debug <boolean:yes|no>")
    }
    scope.isDebug
  }

  private def httpGet(url: String): Future[js.Any] = Request.getFuture[js.Any](url).map(_._2)

  private def httpHead(url: String): Future[js.Any] = Request.headFuture[js.Any](url).map(_._1)

  private def httpPatch(url: String): Future[js.Any] = Request.patchFuture[js.Any](url).map(_._2)

  private def httpPost(args: List[String]): Future[js.Any] = {
    args match {
      case url :: body :: Nil if body.startsWith("{") => Request.postFuture[js.Any](new RequestOptions(url = url, body = JSON.parse(body), json = true)).map(_._2)
      case url :: body :: Nil => Request.postFuture[js.Any](new RequestOptions(url = url, body = body)).map(_._2)
      case url :: Nil => Request.postFuture[js.Any](url).map(_._2)
      case _ => throw js.JavaScriptException("Syntax error; usage: post <url> [<body>]")
    }
  }

  private def httpPut(args: List[String]): Future[js.Any] = {
    args match {
      case url :: body :: Nil if body.startsWith("{") => Request.putFuture[js.Any](new RequestOptions(url = url, body = JSON.parse(body), json = true)).map(_._2)
      case url :: body :: Nil => Request.putFuture[js.Any](new RequestOptions(url = url, body = body)).map(_._2)
      case url :: Nil => Request.putFuture[js.Any](url).map(_._2)
      case _ => throw js.JavaScriptException("Syntax error; usage: put <url> [<body>]")
    }
  }

  private def printCompact(message: String): Future[js.Any] = Future {
    val displayMessage = (if (message.startsWith("\"") && message.endsWith("\"")) message.drop(1).dropRight(1) else message)
      .replaceAllLiterally("\\t", "\t")
      .replaceAllLiterally("\\n", "\n")
      .replaceAllLiterally("\\r", "\r")
    println(displayMessage)
  }

}

/**
 * Command Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Command {

  def apply(name: String, args: List[String], lineNumber: Int, correlationID: Option[String]): Command = {
    new Command(name, args, lineNumber, correlationID)
  }

  def parseCommand(line: String, lineNumber: Int): Command = {
    val chunks = line.safeSplit(delimiter = ' ', limit = Int.MaxValue)
    chunks match {
      case correlationID :: name :: args if correlationID.startsWith("[") & correlationID.endsWith("]") =>
        Command(name, args, lineNumber, Some(correlationID.drop(1).dropRight(1)))
      case name :: args =>
        Command(name, args, lineNumber, correlationID = None)
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
        case _ => throw js.JavaScriptException("A single string argument was expected")
      }

      def oneIdAndJSON[A <: js.Any]: (String, A) = args.toList match {
        case id :: arg :: Nil => (id, JSON.parseAs[A](arg))
        case _ => throw js.JavaScriptException("A single JSON argument was expected")
      }

      def oneJSON[A <: js.Any]: A = args.toList match {
        case arg :: Nil => JSON.parseAs[A](arg)
        case _ => throw js.JavaScriptException("A single JSON argument was expected")
      }

      def oneJSONAndID[A <: js.Any]: (A, String) = args.toList match {
        case arg :: id :: Nil => (JSON.parseAs[A](arg), id)
        case _ => throw js.JavaScriptException("A single JSON argument was expected")
      }

      def oneJSONAndTwoIDs[A <: js.Any]: (A, String, String) = args.toList match {
        case arg :: id0 :: id1 :: Nil => (JSON.parseAs[A](arg), id0, id1)
        case _ => throw js.JavaScriptException("A JSON object and two numeric arguments were expected")
      }

      def oneJSONAndIDAndJSON[A <: js.Any, B <: js.Any]: (A, String, B) = args.toList match {
        case arg0 :: id :: arg1 :: Nil if arg1.startsWith("{") => (JSON.parseAs[A](arg0), id, JSON.parseAs[B](arg1))
        case _ => throw js.JavaScriptException("A JSON object and two numeric arguments were expected")
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
