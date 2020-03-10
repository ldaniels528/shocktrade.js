package com.shocktrade.client.contest

import com.shocktrade.common.models.contest.ChatMessage
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * Chat Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ChatService($http: Http) extends Service {

  def getMessages(contestId: String): js.Promise[HttpResponse[js.Array[ChatMessage]]] = {
    $http.get[js.Array[ChatMessage]](s"/api/contest/$contestId/chat")
  }

  def sendChatMessage(contestId: String, message: ChatMessage): js.Promise[HttpResponse[js.Array[ChatMessage]]] = {
    $http.post[js.Array[ChatMessage]](s"/api/contest/$contestId/chat", message)
  }

}
