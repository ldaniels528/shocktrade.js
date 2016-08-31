package com.shocktrade.javascript.discover

import org.scalajs.angularjs.Location
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Controller, injected}
import com.shocktrade.javascript.Filters.toDuration
import com.shocktrade.javascript.MySessionService
import org.scalajs.nodejs.util.ScalaJsHelper._
import com.shocktrade.javascript.dashboard.ContestService
import com.shocktrade.javascript.discover.ChatController._
import com.shocktrade.javascript.models.contest.{Message, PlayerRef}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
 * Chat Controller
 * @author lawrence.daniels@gmail.com
 */
class ChatController($scope: js.Dynamic, $location: Location, toaster: Toaster,
                     @injected("MySessionService") mySession: MySessionService,
                     @injected("ContestService") contestService: ContestService)
  extends Controller {

  private val colorMap = js.Dictionary[String]()
  private var lastUpdateTime = 0d
  private var lastMessageCount = 0
  private var cachedHtml = ""

  $scope.chatMessage = ""

  $scope.addSmiley = (emoticon: js.Dynamic) => $scope.chatMessage += " " + emoticon.symbol

  $scope.getEmoticons = () => Emoticons

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
    val messages = mySession.getMessages
    if ((messages.length == lastMessageCount) && (js.Date.now() - lastUpdateTime) <= 1000) cachedHtml
    else {
      // capture the start time
      val startTime = js.Date.now()

      // capture the new number of lines
      lastMessageCount = messages.length

      // build an HTML string with emoticons
      val html = messages.foldLeft[String]("") { (html, msg) =>
        // replace the symbols with icon images
        var text = msg.text getOrElse ""
        if(text.nonEmpty) {
          Emoticons.foreach { emo =>
            text = text.replaceAllLiterally(emo.symbol, s"""<img src="assets/images/smilies/${emo.uri}">""")
          }
        }

        val senderName = msg.sender.flatMap(_.name).orNull
        html + s"""<img src="http://graph.facebook.com/${msg.sender.flatMap(_.facebookID)}/picture" class="chat_icon">
                   <span class="bold" style="color: ${colorOf(senderName)}">$senderName</span>&nbsp;
                   [<span class="st_bkg_color">${toDuration(msg.sentTime)}</span>]&nbsp;$text<br>""".stripPrefix(" ")
      }

      //console.log(f"Generated HTML in ${js.Date.now() - startTime}%.1f msec(s)")
      cachedHtml = html
      lastUpdateTime = js.Date.now()
      cachedHtml
    }
  }

  private def colorOf(name: String) = colorMap.getOrElseUpdate(name, Colors((1 + colorMap.size) % Colors.length))

  /**
   * Sends a chat message to the server
   * @param messageText the given chat message text
   */
  private def sendChatMessage(messageText: String) {
    val outcome = for {
      playerId <- mySession.userProfile._id.toOption
      facebookID <- mySession.facebookID.toOption
      contestId <- mySession.contest.flatMap(_._id.toOption)
    } yield (playerId, facebookID, contestId)

    outcome match {
      case Some((playerId, facebookID, contestId)) =>
        if (messageText.trim.nonEmpty) {
          // build the message blob
          val message = New[Message]
          message.text = messageText
          //message.recipient = null
          message.sender = new PlayerRef(_id = playerId, name = mySession.getUserName, facebookID = facebookID)

          // transmit the message
          contestService.sendChatMessage(contestId, message) onComplete {
            case Success(messages) =>
              $scope.chatMessage = ""
              mySession.setMessages(messages)
            case Failure(e) =>
              toaster.error("Failed to send message")
          }
        }
      case None =>
        toaster.error("No game selected")
    }
  }

}

/**
 * Chat Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object ChatController {

  private val Colors = js.Array("#0088ff", "#ff00ff", "#008888", "#2200ff")

  @ScalaJSDefined
  class Emotion(val symbol: String, val uri: String, val tooltip: String) extends js.Object

  private val Emoticons = js.Array(
    new Emotion(symbol = ":-@", uri = "icon_mrgreen.gif", tooltip = "Big Grin"),
    new Emotion(symbol = ":-)", uri = "icon_smile.gif", tooltip = "Smile"),
    new Emotion(symbol = "-)", uri = "icon_wink.gif", tooltip = "Wink"),
    new Emotion(symbol = ":-D", uri = "icon_biggrin.gif", tooltip = "Big Smile"),
    new Emotion(symbol = ":->", uri = "icon_razz.gif", tooltip = "Razzed"),
    new Emotion(symbol = "B-)", uri = "icon_cool.gif", tooltip = "Cool"),
    new Emotion(symbol = "$-|", uri = "icon_rolleyes.gif", tooltip = "Roll Eyes"),
    new Emotion(symbol = "8-|", uri = "icon_eek.gif", tooltip = "Eek"),
    new Emotion(symbol = ":-/", uri = "icon_confused.gif", tooltip = "Confused"),
    new Emotion(symbol = "|-|", uri = "icon_redface.gif", tooltip = "Blush"),
    new Emotion(symbol = ":-(", uri = "icon_sad.gif", tooltip = "Sad"),
    new Emotion(symbol = ":'-(", uri = "icon_cry.gif", tooltip = "Cry"),
    new Emotion(symbol = ">:-(", uri = "icon_evil.gif", tooltip = "Enraged"),
    new Emotion(symbol = ":-|", uri = "icon_neutral.gif", tooltip = "Neutral"),
    new Emotion(symbol = ":-O", uri = "icon_surprised.gif", tooltip = "Surprised"),
    new Emotion(symbol = "(i)", uri = "icon_idea.gif", tooltip = "Idea"),
    new Emotion(symbol = "(!)", uri = "icon_exclaim.gif", tooltip = "Exclamation"),
    new Emotion(symbol = "(?)", uri = "icon_question.gif", tooltip = "Question"),
    new Emotion(symbol = "=>", uri = "icon_arrow.gif", tooltip = "Arrow"))

}
