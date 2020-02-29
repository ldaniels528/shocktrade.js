package com.shocktrade.ingestion.routes

import com.shocktrade.server.common.TradingClock
import com.shocktrade.ingestion.concurrent.Daemon
import com.shocktrade.ingestion.concurrent.Daemon.DaemonRef
import io.scalajs.nodejs._
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
  * Daemon Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object DaemonRoutes {

  def init[T](app: Application, daemons: Seq[DaemonRef[T]])(implicit ec: ExecutionContext): Unit = {
    val clock = new TradingClock()
    val daemonDict = js.Dictionary(daemons.map(d => d.name -> new DaemonJs(d.name, d.delay, d.frequency)): _*)
    val daemonMap = Map(daemons.map(d => d.name -> d): _*)

    // individual objects
    app.get("/api/daemon/:id", (request: Request, response: Response, next: NextFunction) => daemonById(request, response, next))
    app.get("/api/daemon/:id/pause", (request: Request, response: Response, next: NextFunction) => pauseDaemon(request, response, next))
    app.get("/api/daemon/:id/resume", (request: Request, response: Response, next: NextFunction) => resumeDaemon(request, response, next))
    app.get("/api/daemon/:id/start", (request: Request, response: Response, next: NextFunction) => startDaemon(request, response, next))

    // collections
    app.get("/api/daemons", (request: Request, response: Response, next: NextFunction) => listDaemons(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    /**
      * Returns a daemon by its unique identifier
      */
    def daemonById(request: Request, response: Response, next: NextFunction): Unit = {
      val id = request.params("id")
      daemonDict.get(id) match {
        case Some(daemon) => response.send(daemon)
        case None => response.notFound(id)
      }
      next()
    }

    /**
      * Returns the list of configured daemons
      */
    def listDaemons(request: Request, response: Response, next: NextFunction): Unit = {
      response.send(daemonDict.values.toJSArray)
      next()
    }

    /**
      * Pauses the daemon process, if running.
      */
    def pauseDaemon(request: Request, response: Response, next: NextFunction): Unit = {
      val id = request.params("id")
      daemonMap.get(id) match {
        case Some(daemon) =>
          //Daemon.run(clock, daemon)
          response.send(s"${daemon.name} paused")
        case None => response.notFound(id)
      }
      next()
    }

    /**
      * Resume the daemon process, if paused.
      */
    def resumeDaemon(request: Request, response: Response, next: NextFunction): Unit = {
      val id = request.params("id")
      daemonMap.get(id) match {
        case Some(daemon) =>
          //Daemon.run(clock, daemon)
          response.send(s"${daemon.name} resumed")
        case None => response.notFound(id)
      }
      next()
    }

    /**
      * Starts the daemon process
      */
    def startDaemon(request: Request, response: Response, next: NextFunction): Unit = {
      val id = request.params("id")
      daemonMap.get(id) match {
        case Some(daemon) =>
          Daemon.start(clock, daemon)
          response.send(s"${daemon.name} started")
        case None => response.notFound(id)
      }
      next()
    }

  }

  /**
    * The JSON representation of a daemon reference
    * @param name      the name of the daemon
    * @param delay     the initial delay before the process runs
    * @param frequency the frequency/interval of the process
    */
  class DaemonJs(val name: String, val delay: Int, val frequency: Int) extends js.Object

}
