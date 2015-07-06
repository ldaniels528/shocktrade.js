package com.shocktrade.javascript

import ScalaJsHelper._
import com.ldaniels528.scalascript.core.{Http, Location, Timeout}
import com.ldaniels528.scalascript.extensions.Toaster
import com.ldaniels528.scalascript.{Service, injected}
import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, MessageEvent}
import org.scalajs.dom.{Event, WebSocket}
import org.scalajs.dom.console

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExportAll

/**
 * Web Socket Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@JSExportAll
class WebSocketService($rootScope: js.Dynamic, $http: Http, $location: Location, $timeout: Timeout, toaster: Toaster,
                       @injected("MySession") mySession: MySession)
  extends Service {

  private var socket: WebSocket = null
  private var connected = false
  private var attemptsLeft = 3

  /**
   * Initializes the service
   */
  def init() {
    console.log("Initializing Websocket service...")
    if (!isDefined(g.window.WebSocket)) {
      console.log("Using a Mozilla Web Socket")
      g.window.WebSocket = g.window.MozWebSocket
    }

    if (isDefined(g.window.WebSocket)) connect()
    else
      toaster.pop("Info", "Your browser does not support Web Sockets.", null)
  }

  /**
   * Indicates whether a connection is established
   */
  def isConnected: js.Function0[Boolean] = () => connected

  /**
   * Transmits the message to the server via web-socket
   */
  def send: js.Function = (message: String) => {
    if (!isDefined(g.window.WebSocket)) {
      toaster.error("Web socket closed")
      false
    }
    if (socket.readyState == WebSocket.OPEN) {
      socket.send(message)
      true
    } else {
      toaster.error(s"Web socket closed: readyState = ${socket.readyState}")
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
        console.log(s"Broadcasting action '${message.action}'")
        $rootScope.$broadcast(message.action, message.data)
      }
      else g.console.warning(s"Message does not contain an action message = ${JSON.stringify(message)}")
    }
    else g.console.warning(s"Unhandled event received - ${JSON.stringify(event)}")
  }

  private def sendState(connected: Boolean) {
    mySession.userProfile.OID_? match {
      case Some(userID) =>
        console.log(s"Sending connected status for user $userID ...")
        if (connected) $http.put(s"/api/online/$userID")
        else $http.delete(s"/api/online/$userID")
      case None =>
        console.log(s"User unknown, waiting 5 seconds ($attemptsLeft attempts remaining)...")
        if (attemptsLeft > 0) {
          $timeout(() => sendState(connected), 5000)
          attemptsLeft -= 1
        }
    }
  }

  /**
   * Establishes a web socket connection
   */
  private def connect() {
    val endpoint = s"ws://${$location.host()}:${$location.port()}/websocket"
    console.log(s"Connecting to websocket endpoint '$endpoint'...")

    // open the connection and setup the handlers
    socket = new WebSocket(endpoint)

    socket.onopen = (event: Event) => {
      connected = true
      sendState(connected)
      console.log("Websocket connection established")
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
