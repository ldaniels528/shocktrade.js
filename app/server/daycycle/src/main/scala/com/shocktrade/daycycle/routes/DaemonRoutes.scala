package com.shocktrade.daycycle.routes

import com.shocktrade.concurrent.Daemon
import com.shocktrade.concurrent.Daemon.DaemonRef
import com.shocktrade.serverside.TradingClock
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}
import org.scalajs.nodejs.{NodeRequire, duration2Int}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Daemon Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object DaemonRoutes {

  def init(app: Application, daemons: Seq[DaemonRef], dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) = {
    val clock = new TradingClock()
    val daemonDict = js.Dictionary(daemons.map(d => d.id -> new DaemonJs(d.id, d.name, d.delay, d.frequency)): _*)
    val daemonMap = Map(daemons.map(d => d.id -> d): _*)

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
    def daemonById(request: Request, response: Response, next: NextFunction) = {
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
    def listDaemons(request: Request, response: Response, next: NextFunction) = {
      response.send(daemonDict.values.toJSArray)
      next()
    }

    /**
      * Pauses the daemon process, if running.
      */
    def pauseDaemon(request: Request, response: Response, next: NextFunction) = {
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
    def resumeDaemon(request: Request, response: Response, next: NextFunction) = {
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
    def startDaemon(request: Request, response: Response, next: NextFunction) = {
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
    * @param id        the given daemon identifier
    * @param name      the name of the daemon
    * @param delay     the initial delay before the process runs
    * @param frequency the frequency/interval of the process
    */
  @ScalaJSDefined
  class DaemonJs(val id: String, val name: String, val delay: Int, val frequency: Int) extends js.Object

}
