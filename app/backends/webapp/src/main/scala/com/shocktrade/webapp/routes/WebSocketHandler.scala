package com.shocktrade.webapp.routes

import java.util.UUID

import com.shocktrade.common.events.RemoteEvent
import com.shocktrade.server.common.LoggerFactory
import io.scalajs.nodejs._
import io.scalajs.nodejs.timers.Immediate
import io.scalajs.npm.express.Request
import io.scalajs.npm.expressws.WebSocket

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success, Try}

/**
 * WebSocket Handler
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object WebSocketHandler {
  private val logger = LoggerFactory.getLogger(getClass)
  private val clients = js.Array[WsClient]()

  def messageHandler(ws: WebSocket, request: Request, message: String): Unit = {
    // handle the message
    message match {
      case "Hello" =>
        // have we received a message from this client before?
        val client = new WsClient(ip = request.ip, ws = ws)
        logger.log(s"Client ${client.uid} (${client.ip}) connected")
        clients.push(client)
      case unknown =>
        logger.warn(s"Unhandled message '$unknown'...")
    }
    ()
  }

  def emit(remoteEvent: RemoteEvent): Immediate = {
    setImmediate { () =>
      //logger.log(s"Broadcasting action '$action' with data '$data'...")
      clients.foreach(client => Try(client.send(remoteEvent)) match {
        case Success(_) =>
        case Failure(e) =>
          logger.warn(s"Client connection ${client.uid} (${client.ip}) failed")
          clients.indexWhere(_.uid == client.uid) match {
            case -1 => logger.error(s"Client ${client.uid} was not removed")
            case index =>
              logger.info(s"Dropping client #$index [${client.uid}]...")
              clients.remove(index)
          }
      })
    }
  }

  /**
   * Represents a web-socket client
   * @param ws the given [[WebSocket web socket]]
   */
  class WsClient(val ip: String, val ws: WebSocket) {
    val uid: EventType = UUID.randomUUID().toString

    def send(remoteEvent: RemoteEvent): js.Any = ws.send(JSON.stringify(remoteEvent))

  }

}
