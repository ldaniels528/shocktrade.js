package com.shocktrade.client

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.users.UserService
import com.shocktrade.common.events.RemoteEvent
import io.scalajs.JSON
import io.scalajs.dom.html.browser.{console, window}
import io.scalajs.dom.ws._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Location, Service, Timeout, _}
import io.scalajs.util.DurationHelper._

import scala.concurrent.duration._
import scala.scalajs.js

/**
 * WebSocket Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class WebSocketService($rootScope: RootScope, $location: Location, $timeout: Timeout, toaster: Toaster,
                       @injected("UserService") userService: UserService) extends Service {
  private var socket: WebSocket = _
  private var connected: Boolean = false
  private var attemptsLeft: Int = 3

  /**
   * Initializes the service
   */
  def init(): Unit = {
    console.log("Initializing WebSocket service...")
    if (window.WebSocket.isEmpty) {
      console.log("Using a Mozilla Web Socket")
      window.WebSocket = window.MozWebSocket
    }

    window.WebSocket.toOption match {
      case Some(_) => connect()
      case None =>
        toaster.pop(`type` = "Info", title = "Your browser does not support Web Sockets.")
    }
  }

  /**
   * Indicates whether a connection is established
   */
  def isConnected: Boolean = Option(socket).nonEmpty && connected

  /**
   * Transmits the message to the server via web-socket
   * @param message the given message
   */
  def send(message: String): Boolean = {
    window.WebSocket.toOption match {
      case Some(_) if socket.readyState == WebSocket.OPEN =>
        socket.send(message)
        true
      case Some(_) =>
        toaster.error(s"Web socket closed: readyState = ${socket.readyState}")
        false
      case None =>
        toaster.error("Web socket closed: window.WebSocket is not defined")
        false
    }
  }

  /**
   * Establishes a web socket connection
   */
  private def connect(): Unit = {
    val endpoint = s"ws://${$location.host()}:${$location.port()}/websocket"
    console.log(s"Connecting to WebSocket endpoint '$endpoint'...")

    // open the connection and setup the handlers
    socket = new WebSocket(endpoint)

    socket.onopen = (event: OpenEvent) => {
      connected = true
      sendState(connected)
      console.log("WebSocket connection established")
    }

    socket.onclose = (event: CloseEvent) => {
      connected = false
      sendState(connected)
      console.warn("WebSocket connection lost")
      $timeout(() => connect(), 15.seconds)
    }

    socket.onerror = (event: ErrorEvent) => ()

    socket.onmessage = (event: MessageEvent) => handleMessage(event)

    $timeout(() => send("Hello"), 5.seconds)
  }

  /**
   * Handles the incoming web socket message event
   * @param event the given web socket message event
   */
  private def handleMessage(event: MessageEvent): Unit = {
    Option(event.data) match {
      case Some(rawMessage: String) =>
        val message = JSON.parseAs[RemoteEvent](rawMessage)
        val result = for {
          action <- message.action.toOption
          data <- message.data.toOption
        } yield (action, data)
        result match {
          case Some((action, data)) =>
            console.log(s"WSS| action: $action")
            $rootScope.emit(action, JSON.parse(data))
          case None =>
            console.warn(s"Message does not contain either an 'action' or 'data' property: ${angular.toJson(message)}")
        }
      case Some(data) =>
        console.warn(s"Unrecognized event data type: ${angular.toJson(data.asInstanceOf[js.Any])}")
      case None =>
        console.warn(s"Unhandled event received: ${angular.toJson(event)}")
    }
  }

  /**
   * Transmits the current "connected" state of the user
   * @param connected the given connection status indicator
   */
  private def sendState(connected: Boolean): Unit = {
    $rootScope.userProfile.flatMap(_.userID).toOption match {
      case Some(userID) =>
        console.log(s"Sending connected status for user $userID ...")
        if (connected) userService.setIsOnline(userID) else userService.setIsOffline(userID)
      case None =>
        console.log(s"User unknown, waiting 5 seconds ($attemptsLeft attempts remaining)...")
        if (attemptsLeft > 0) {
          $timeout(() => sendState(connected), 5.seconds)
          attemptsLeft -= 1
        }
    }
  }

}
