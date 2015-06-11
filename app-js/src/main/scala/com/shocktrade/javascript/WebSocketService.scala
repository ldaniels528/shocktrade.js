package com.shocktrade.javascript

import biz.enef.angulate.core.{HttpService, Location, Timeout}
import biz.enef.angulate.{Service, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.ScalaJsHelper._
import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, MessageEvent}
import org.scalajs.dom.{Event, WebSocket}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExportAll

/**
 * Web Socket Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@JSExportAll
class WebSocketService($rootScope: js.Dynamic, $http: HttpService, $location: Location, $timeout: Timeout, toaster: Toaster,
                       @named("MySession") mySession: MySession) extends Service {
  private var socket: WebSocket = null
  private var connected = false

  /**
   * Initializes the service
   */
  def init() {
    g.console.log("Initializing Websocket service...")
    if (!isDefined(g.window.WebSocket)) {
      g.console.log("Using a Mozilla Web Socket")
      g.window.WebSocket = g.window.MozWebSocket
    }

    if (isDefined(g.window.WebSocket)) connect()
    else
      toaster.pop("Info", "Your browser does not support Web Sockets.", null)
  }

  /**
   * Indicates whether a connection is established
   */
  def isConnected: js.Function = () => connected

  /**
   * Transmits the message to the server via web-socket
   */
  def send: js.Function = (message: String) => {
    if (!isDefined(g.window.WebSocket)) {
      toaster.pop("error", "Online Status", "Web socket closed")
      false
    }
    if (socket.readyState == WebSocket.OPEN) {
      socket.send(message)
      true
    } else {
      toaster.pop("error", "Online Status", "Web socket closed: readyState = " + socket.readyState)
      false
    }
  }

  /**
   * Handles the incoming web socket message event
   * @param event the given web socket message event
   */
  private def handleMessage(event: MessageEvent) {
    if (event.data != null) {
      val message = JSON.parse(event.data.asInstanceOf[String])
      if (isDefined(message.action)) {
        g.console.log(s"Broadcasting action '${message.action}'")
        $rootScope.$broadcast(message.action, message.data)
      }
      else g.console.warning(s"Message does not contain an action message = ${JSON.stringify(message)}")
    }
    else g.console.warning(s"Unhandled event received - ${JSON.stringify(event)}")
  }

  private def sendState(connected: Boolean) {
    mySession.userProfile.OID_? match {
      case Some(userID) =>
        g.console.log(s"Sending connected status for user $userID ...")
        if (connected) $http.put(s"/api/online/$userID")
        else $http.delete(s"/api/online/$userID")
      case None =>
        g.console.log("User unknown, waiting 5 seconds...")
        $timeout(() => sendState(connected), 5000)
    }
  }

  /**
   * Establishes a web socket connection
   */
  private def connect() {
    val endpoint = s"ws://${$location.host()}:${$location.port()}/websocket"
    g.console.log(s"Connecting to websocket endpoint '$endpoint'...")

    // open the connection and setup the handlers
    socket = new WebSocket(endpoint)

    socket.onopen = (event: Event) => {
      connected = true
      sendState(connected)
      g.console.log("Websocket connection established")
    }

    socket.onclose = (event: CloseEvent) => {
      connected = false
      sendState(connected)
      g.console.warn("Websocket connection lost")
      $timeout(() => connect(), 15000)
    }

    socket.onerror = (event: ErrorEvent) => ()

    socket.onmessage = (event: MessageEvent) => handleMessage(event)
  }

}
