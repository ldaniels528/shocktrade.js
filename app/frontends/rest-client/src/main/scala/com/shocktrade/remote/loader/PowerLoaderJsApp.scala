package com.shocktrade.remote.loader

import com.shocktrade.remote.proxies._
import com.shocktrade.server.common.LoggerFactory

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.JSExport

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
      case _ => ("localhost", 9000, "./scripts/restless/cqm-test.js")
      //js.JavaScriptException(s"Syntax: ${getClass.getSimpleName} <host> <port> <inputPath>")
    }

    // create the proxies
    implicit val c: ContestProxy = new ContestProxy(host, port)
    implicit val p: PortfolioProxy = new PortfolioProxy(host, port)
    implicit val u: UserProxy = new UserProxy(host, port)

    // create tge scope
    implicit val scope: Scope = new Scope()

    // start the processor
   new CommandProcessor().start(inputFile)
    ()
  }

}