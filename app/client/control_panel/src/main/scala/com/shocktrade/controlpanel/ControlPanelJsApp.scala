package com.shocktrade.controlpanel

import com.shocktrade.server.common.LoggerFactory
import org.scalajs.dom._
import org.scalajs.nodejs.Bootstrap
import org.scalajs.nodejs.globals.process
import org.scalajs.nodejs.readline.{Readline, ReadlineOptions}
import org.scalajs.nodejs.request.Request
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExportAll

/**
  * Control Panel JavaScript Application
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@JSExportAll
object ControlPanelJsApp extends js.JSApp {
  private val logger = LoggerFactory.getLogger(getClass)
  private val compiler = Compiler()

  override def main() {}

  def startServer(implicit bootstrap: Bootstrap) {
    implicit val require = bootstrap.require

    logger.info("Starting the ShockTrade Control Panel...")

    implicit val request = Request()

    val readline = Readline()
    val rl = readline.createInterface(new ReadlineOptions(input = process.stdin, output = process.stdout))

    rl.setPrompt("daemons#> ")
    rl.prompt()

    rl.onLine { line =>
      line.trim() match {
        case s if s.isEmpty => ()
        case "daemons" => listDaemons()
        case "exit" => rl.close()
        case command =>
          parseCommand(command)
        //console.log(s"Syntax error: '$command' command not found")
      }
      rl.prompt()
    }

    rl.onClose { () =>
      logger.log("Shutting down...")
      process.exit(0)
    }
  }

  private def parseCommand(line: String) = {
    compiler.compile(line)
  }

  private def listDaemons(remote: String = "localhost:1337")(implicit request: Request) = {
    request.getFuture(s"http://$remote/api/daemons") map { case (response, data) =>
      console.log(pretty(data))
    }
  }

  private def pretty(data: String) = {
    JSON.dynamic.stringify(JSON.parse(data), null, "\t").asInstanceOf[String]
  }

}
