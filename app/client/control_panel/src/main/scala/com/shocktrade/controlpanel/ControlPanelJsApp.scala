package com.shocktrade.controlpanel

import com.shocktrade.controlpanel.runtime._
import com.shocktrade.controlpanel.runtime.functions.builtin.BuiltinFunctions
import com.shocktrade.server.common.LoggerFactory
import io.scalajs.nodejs.readline.{Readline, ReadlineOptions}
import io.scalajs.nodejs.{console, process}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.{JSExport, JSExportAll}
import scala.util.{Failure, Success, Try}

/**
  * Control Panel JavaScript Application
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ControlPanelJsApp {
  private val logger = LoggerFactory.getLogger(getClass)
  private val compiler = Compiler()

  @JSExport
  def main(args: Array[String]): Unit = {
    logger.info("Starting the ShockTrade Control Panel...")

    val host = process.env.getOrElse("host", "localhost:1337")
    val globalScope = new RootScope()
    BuiltinFunctions.enrich(globalScope)

    val rl = Readline.createInterface(new ReadlineOptions(input = process.stdin, output = process.stdout))
    val rc = new RuntimeContext(rl.close())

    rl.setPrompt(s"$host#> ")
    rl.prompt()

    rl.onLine { line =>
      line.trim() match {
        case s if s.isEmpty => ()
        case command =>
          interpretCommand(rc, globalScope, command)
      }
      rl.prompt()
    }

    rl.onClose { () =>
      logger.log("Shutting down...")
      process.exit(0)
    }

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      logger.error("An uncaught exception was fired:")
      logger.error(err.stack)
    }

  }

  private def interpretCommand(rc: RuntimeContext, scope: Scope, line: String) = {
    Try(compiler.compile(line)) match {
      case Success(executable) =>
        val result = executable.eval(rc, scope)
        handleResult(result)
      case Failure(e) =>
        console.error(e.getMessage)
    }
  }

  private def handleResult(promise: Future[TypedValue]) = {
    promise onComplete {
      case Success(value) => println(value)
      case Failure(e) =>
        console.error(e.getMessage)
    }
  }

}
