package com.shocktrade.remote.loader

import com.shocktrade.remote.proxies._
import com.shocktrade.server.common.LoggerFactory
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.process
import io.scalajs.nodejs.readline.{Readline, ReadlineOptions}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
 * ShockTrade Power Loader JavaScript Application
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PowerLoaderJsApp {
  private val logger = LoggerFactory.getLogger(getClass)
  private val version = 0.1

  @JSExport
  def main(args: Array[String]): Unit = {
    logger.info(s"ShockTrade Power Loader v$version")

    // get the input arguments
    val (host, port, inputFile) = args.toList match {
      case host :: port :: path :: Nil => (host, port.toInt, path)
      case _ => ("localhost", 9000, "./replayer2.txt")
      //js.JavaScriptException(s"Syntax: ${getClass.getSimpleName} <host> <port> <inputPath>")
    }

    // create the proxies
    implicit val c: ContestProxy = new ContestProxy(host, port)
    implicit val p: PortfolioProxy = new PortfolioProxy(host, port)
    implicit val u: UserProxy = new UserProxy(host, port)

    // create tge scope
    implicit val scope: Scope = new Scope()

    // start the loader
    start(inputFile)
    ()
  }

  def start(inputFile: String)(implicit c: ContestProxy, p: PortfolioProxy, u: UserProxy, scope: Scope): Int = {
    // create the reader
    val readInterface = Readline.createInterface(new ReadlineOptions(
      input = Fs.createReadStream(inputFile),
      output = process.stdout,
      console = true
    ))

    // process the file
    var lineNumber = 0
    readInterface.onLine { raw =>
      lineNumber += 1
      val line = raw.trim

      // ignore comments
      if (!line.startsWith("#") && line.nonEmpty) {
        // parse the command
        val command = Command.parseCommand(line)

        // create the asynchronous task
        val task = createTask(command, line, lineNumber)
        if (scope.isDebug) logger.info(f"[$lineNumber%04d] $line [queued]")

        // place the task on the stack if there's a correlationID
        command.correlationID foreach (scope.add(_, task))

        // handle the promise
        task.promise onComplete {
          case Success(result) =>
          //logger.info(f"[${task.lineNumber}%04d] ${task.toString} - Success: ${JSON.stringify(result, null, 4)}")
          case Failure(e) =>
            logger.error(f"[${task.lineNumber}%04d] ${task.toString} - Failed: ${e.getMessage} - [$line]")
        }
      }
    }
    lineNumber
  }

  private def createTask(command: Command, commandLine: String, lineNumber: Int)
                        (implicit c: ContestProxy, p: PortfolioProxy, u: UserProxy, scope: Scope): Task = {
    // were variables specified?
    val variableRefs = Scope.findVariables(commandLine)

    // are there task dependencies?
    if (variableRefs.isEmpty) Task(command, lineNumber, command.invoke())

    // create a dependent task
    else {
      val dependencies = variableRefs.map(_.getInstanceKey) map scope.getTask
      val outcome = for {
        values <- Future.sequence(for {
          dependency <- dependencies
          correlationID <- dependency.correlationID.toList
          tuple = dependency.promise.map(promise => (correlationID, promise))
        } yield tuple).map(js.Dictionary[js.Any](_: _*))

        updatedLine = variableRefs.foldLeft(commandLine) { (line, variable) => variable.replaceTags(line, values) }
        updatedCommand = Command.parseCommand(updatedLine)
        result <- updatedCommand.invoke()
      } yield result

      if (scope.isDebug) logger.info(f"[$lineNumber%04d] $commandLine [${dependencies.length} dependencies]")
      Task(command, lineNumber, outcome)
    }
  }

}