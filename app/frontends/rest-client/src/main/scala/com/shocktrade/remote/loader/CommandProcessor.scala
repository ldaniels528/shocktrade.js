package com.shocktrade.remote.loader

import com.shocktrade.remote.proxies.{ContestProxy, PortfolioProxy, UserProxy}
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.process
import io.scalajs.nodejs.readline.{Readline, ReadlineOptions}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Command Processor
 * @param c     the implicit [[ContestProxy]]
 * @param p     the implicit [[PortfolioProxy]]
 * @param u     the implicit [[UserProxy]]
 * @param scope the implicit [[Scope]]
 * @param ec    the implicit [[ExecutionContext]]
 */
class CommandProcessor()(implicit c: ContestProxy, p: PortfolioProxy, u: UserProxy, scope: Scope, ec: ExecutionContext) {

  def start(inputFile: String): Int = {
    // create the reader
    val readInterface = Readline.createInterface(new ReadlineOptions(
      input = Fs.createReadStream(inputFile),
      output = process.stdout,
      console = true
    ))

    // process the file
    var lineNumber = 0
    readInterface.onLine { rawLine =>
      lineNumber += 1
      val line = rawLine.trim

      // ignore comments
      if (!line.startsWith("#") && !line.startsWith("//") && line.nonEmpty) {
        // parse the command
        val command = Command.parseCommand(line, lineNumber)

        // create the asynchronous task
        val task = createTask(command, line)
        command.info(s"$line [queued]")

        // place the task in the scope if there's a correlation ID
        command.correlationID foreach (scope.add(_, task))

        // handle the promise
        task.promise onComplete {
          case Success(result) if !js.isUndefined(result) =>
            command.info(s"${task.toString} - Success: ${JSON.stringify(result)}")
          case Success(_) =>
            command.info(s"${task.toString} - Success: [undefined]")
          case Failure(e) =>
            command.error(s"${task.toString} - Failed: ${e.getMessage} - [$line]")
        }
      }
    }
    lineNumber
  }

  private def createTask(command: Command, commandLine: String): Task = {
    // were variables specified?
    val variableRefs = Scope.findVariables(commandLine)

    // are there task dependencies?
    if (variableRefs.isEmpty) Task(command, command.invoke())

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
        updatedCommand = Command.parseCommand(updatedLine, command.lineNumber)
        result <- updatedCommand.invoke()
      } yield result

      command.info(s"$commandLine [${dependencies.length} dependencies]")
      Task(command, outcome)
    }
  }

}
