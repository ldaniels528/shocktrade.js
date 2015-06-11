package com.shocktrade.javascript.news

import biz.enef.angulate.core.{HttpError, Location}
import biz.enef.angulate.{ScopeController, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.Filters
import com.shocktrade.javascript.ScalaJsHelper._
import Filters.toDuration
import com.shocktrade.javascript.dashboard.ContestService

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}

/**
 * Chat Controller
 * @author lawrence.daniels@gmail.com
 */
class ChatController($scope: js.Dynamic, $location: Location, toaster: Toaster,
                     @named("MySession") mySession: js.Dynamic,
                     @named("ContestService") contestService: ContestService) extends ScopeController {
  private val colors = js.Array("#0088ff", "#ff00ff", "#008888", "#2200ff")
  private val colorMap = js.Dictionary[String]()
  private var lastUpdateTime = 0d
  private var lastMessageCount = 0
  private var cachedHtml = ""

  $scope.chatMessage = ""

  $scope.addSmiley = (emoticon: js.Dynamic) => $scope.chatMessage += " " + emoticon.symbol

  $scope.getEmoticons = () => emoticons

  $scope.getMessages = () => getMessages

  $scope.sendChatMessage = (messageText: String) => sendChatMessage(messageText)

  /////////////////////////////////////////////////////////////////////////////
  //			Local Functions and Data
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns chat messages sorted by time
   * @return an HTML string
   */
  private def getMessages: String = {
    val messages = mySession.getMessages().asArray[js.Dynamic]
    if ((messages.length == lastMessageCount) && (js.Date.now() - lastUpdateTime) <= 1000) cachedHtml
    else {
      // capture the start time
      val startTime = js.Date.now()

      // capture the new number of lines
      lastMessageCount = messages.length

      // build an HTML string with emoticons
      val html = sortMessagesByTime(messages).foldLeft[String]("") { (html, msg) =>
        // replace the symbols with icon images
        var text = msg.text.as[String]
        emoticons.foreach { emo =>
         text = text.replaceAllLiterally(emo.symbol.as[String], s"""<img src="assets/images/smilies/${emo.uri.as[String]}">""")
        }

        html + s"""|<img src="http://graph.facebook.com/${msg.sender.facebookID}/picture" class="chat_icon">
                   |<span class="bold" style="color: ${colorOf(msg.sender.name.as[String])}">${msg.sender.name}</span>&nbsp;
                   |[<span class="st_bkg_color">${toDuration(msg.sentTime)}</span>]&nbsp;$text<br>""".stripMargin
      }

      g.console.log(f"Generated HTML in ${js.Date.now() - startTime}%.1f msec(s)")
      cachedHtml = html
      lastUpdateTime = js.Date.now()
      cachedHtml
    }
  }

  private def sortMessagesByTime(messages: js.Array[js.Dynamic]) = messages.sort ({ (a: js.Dynamic, b: js.Dynamic) =>
    val timeA = if (!js.isUndefined(a.sentTime.$date)) a.sentTime.$date else a.sentTime
    val timeB = if (!js.isUndefined(b.sentTime.$date)) b.sentTime.$date else b.sentTime
    (timeB.asInstanceOf[Double] - timeA.asInstanceOf[Double]).toInt
  })

  private def colorOf(name: String) = colorMap.getOrElseUpdate(name, colors((1 + colorMap.size) % colors.length))

  /**
   * Sends a chat message to the server
   * @param messageText the given chat message text
   */
  private def sendChatMessage(messageText: String) {
    if (messageText.trim.nonEmpty) {
      // build the message blob
      var message = JS(
        text = messageText,
        //"recipient": null,
        sender = JS(
          _id = JS($oid = mySession.getUserID()),
          name = mySession.getUserName(),
          facebookID = mySession.getFacebookID()
        ))

      // transmit the message
      contestService.sendChatMessage_@(mySession.getContestID().as[String], message)
        .success { messages: js.Dynamic =>
        $scope.chatMessage = ""
        mySession.setMessages(messages)
      }.error { err: HttpError =>
        toaster.pop("error", "Failed to send message", null)
      }
    }
  }

  private val emoticons = js.Array(
    JS(symbol = ":-@", uri = "icon_mrgreen.gif", tooltip = "Big Grin"),
    JS(symbol = ":-)", uri = "icon_smile.gif", tooltip = "Smile"),
    JS(symbol = "-)", uri = "icon_wink.gif", tooltip = "Wink"),
    JS(symbol = ":-D", uri = "icon_biggrin.gif", tooltip = "Big Smile"),
    JS(symbol = ":->", uri = "icon_razz.gif", tooltip = "Razzed"),
    JS(symbol = "B-)", uri = "icon_cool.gif", tooltip = "Cool"),
    JS(symbol = "$-|", uri = "icon_rolleyes.gif", tooltip = "Roll Eyes"),
    JS(symbol = "8-|", uri = "icon_eek.gif", tooltip = "Eek"),
    JS(symbol = ":-/", uri = "icon_confused.gif", tooltip = "Confused"),
    JS(symbol = "|-|", uri = "icon_redface.gif", tooltip = "Blush"),
    JS(symbol = ":-(", uri = "icon_sad.gif", tooltip = "Sad"),
    JS(symbol = ":'-(", uri = "icon_cry.gif", tooltip = "Cry"),
    JS(symbol = ">:-(", uri = "icon_evil.gif", tooltip = "Enraged"),
    JS(symbol = ":-|", uri = "icon_neutral.gif", tooltip = "Neutral"),
    JS(symbol = ":-O", uri = "icon_surprised.gif", tooltip = "Surprised"),
    JS(symbol = "(i)", uri = "icon_idea.gif", tooltip = "Idea"),
    JS(symbol = "(!)", uri = "icon_exclaim.gif", tooltip = "Exclamation"),
    JS(symbol = "(?)", uri = "icon_question.gif", tooltip = "Question"),
    JS(symbol = "=>", uri = "icon_arrow.gif", tooltip = "Arrow"))

}
