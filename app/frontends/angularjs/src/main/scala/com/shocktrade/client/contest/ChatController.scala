package com.shocktrade.client.contest

import com.shocktrade.client.ContestFactory
import com.shocktrade.client.Filters.toDuration
import com.shocktrade.client.contest.ChatController._
import com.shocktrade.common.models.contest.ChatMessage
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.anchorscroll.AnchorScroll
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Location, Timeout, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Chat Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ChatController($scope: ChatControllerScope, $anchorScroll: AnchorScroll, $location: Location, $timeout: Timeout, toaster: Toaster,
                     @injected("ContestFactory") contestFactory: ContestFactory,
                     @injected("ContestService") contestService: ContestService)
  extends Controller {

  private val colorMap = js.Dictionary[String]()
  private var lastUpdateTime = 0d
  private var lastMessageCount = -1
  private var cachedHtml = ""

  $scope.chatMessage = ""

  /////////////////////////////////////////////////////////////////
  //    Scope Methods
  /////////////////////////////////////////////////////////////////

  $scope.addSmiley = (aEmoticon: js.UndefOr[Emoticon]) => {
    aEmoticon.foreach(emoticon => $scope.chatMessage += " " + emoticon.symbol)
  }

  $scope.getChatMessages = () => {
    val chatMessages = $scope.contest.flatMap(_.messages).getOrElse(js.Array())
    if ((chatMessages.length == lastMessageCount) && (js.Date.now() - lastUpdateTime) <= 10000) cachedHtml
    else {
      // capture the new number of lines
      lastMessageCount = chatMessages.length

      // build an HTML string with emoticons
      val html = chatMessages.foldLeft[String]("") { (html, msg) =>
        // replace the symbols with icon images
        var text = msg.message getOrElse ""
        if (text.nonEmpty) {
          Emoticons.foreach { emo =>
            text = text.replaceAllLiterally(emo.symbol, s"""<img src="/images/smilies/${emo.uri}">""")
          }
        }

        val senderName = msg.username.orNull
        s"""|$html
            |<img src="/api/user/${msg.userID}/icon" class="chat_icon">
            |<span class="bold" style="color: ${colorOf(senderName)}">$senderName</span>&nbsp;
            |[<span class="st_bkg_color">${toDuration(msg.creationTime)}</span>]&nbsp;$text<br>
            |""".stripMargin
      }

      //console.log(f"Generated HTML in ${js.Date.now() - startTime}%.1f msec(s)")
      cachedHtml = html + """<span id="end_of_message"></span>"""
      lastUpdateTime = js.Date.now()
      cachedHtml
    }
  }

  $scope.getEmoticons = () => Emoticons

  $scope.sendChatMessage = (aMessageText: js.UndefOr[String]) => {
    aMessageText foreach sendChatMessage
    $anchorScroll()
    ()
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Local Functions and Data
  /////////////////////////////////////////////////////////////////////////////

  private def colorOf(name: String): String = colorMap.getOrElseUpdate(name, Colors((1 + colorMap.size) % Colors.length))

  /**
   * Sends a chat message to the server
   * @param messageText the given chat message text
   */
  private def sendChatMessage(messageText: String) {
    val outcome = for {
      userID <- $scope.userProfile.flatMap(_.userID).toOption
      contestId <- $scope.contest.flatMap(_.contestID).toOption
    } yield (userID, contestId)

    outcome match {
      case Some((userID, contestID)) =>
        if (messageText.trim.nonEmpty) {
          // build the message blob
          val message = new ChatMessage(userID = userID, username = $scope.userProfile.flatMap(_.username), message = messageText)

          // transmit the message
          val outcome = for {
            _ <- contestService.sendChatMessage(contestID, userID, message)
            response <- contestFactory.refreshMessages(contestID)
          } yield response

          outcome onComplete {
            case Success(response) =>
              $scope.$apply { () => $scope.chatMessage = "" }
            case Failure(e) =>
              toaster.error("Failed to send message")
              console.error(s"Failed to send message: ${e.displayMessage}")
          }
        }
      case None =>
        toaster.error("No game selected")
    }
  }

}

/**
 * Chat Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ChatController {
  private val Colors = js.Array("#0088ff", "#ff00ff", "#008888", "#2200ff")
  private val Emoticons = js.Array(
    new Emoticon(symbol = ">:-(", uri = "icon_evil.gif", tooltip = "Enraged"),
    new Emoticon(symbol = ":-@", uri = "icon_mrgreen.gif", tooltip = "Big Grin"),
    new Emoticon(symbol = ":-)", uri = "icon_smile.gif", tooltip = "Smile"),
    new Emoticon(symbol = ":-D", uri = "icon_biggrin.gif", tooltip = "Big Smile"),
    new Emoticon(symbol = ":->", uri = "icon_razz.gif", tooltip = "Razzed"),
    new Emoticon(symbol = "B-)", uri = "icon_cool.gif", tooltip = "Cool"),
    new Emoticon(symbol = "$-|", uri = "icon_rolleyes.gif", tooltip = "Roll Eyes"),
    new Emoticon(symbol = "8-|", uri = "icon_eek.gif", tooltip = "Eek"),
    new Emoticon(symbol = ":-/", uri = "icon_confused.gif", tooltip = "Confused"),
    new Emoticon(symbol = "|-|", uri = "icon_redface.gif", tooltip = "Blush"),
    new Emoticon(symbol = ":-(", uri = "icon_sad.gif", tooltip = "Sad"),
    new Emoticon(symbol = ":'-(", uri = "icon_cry.gif", tooltip = "Cry"),
    new Emoticon(symbol = ":-|", uri = "icon_neutral.gif", tooltip = "Neutral"),
    new Emoticon(symbol = ":-O", uri = "icon_surprised.gif", tooltip = "Surprised"),
    new Emoticon(symbol = "-)", uri = "icon_wink.gif", tooltip = "Wink"),
    new Emoticon(symbol = "(i)", uri = "icon_idea.gif", tooltip = "Idea"),
    new Emoticon(symbol = "(!)", uri = "icon_exclaim.gif", tooltip = "Exclamation"),
    new Emoticon(symbol = "(?)", uri = "icon_question.gif", tooltip = "Question"),
    new Emoticon(symbol = "=>", uri = "icon_arrow.gif", tooltip = "Arrow"))

  class Emoticon(val symbol: String, val uri: String, val tooltip: String) extends js.Object

}

/**
 * Chat Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait ChatControllerScope extends DashboardScope {
  // variables
  var chatMessage: String = js.native

  // functions
  var addSmiley: js.Function1[js.UndefOr[Emoticon], Unit] = js.native
  var getEmoticons: js.Function0[js.Array[Emoticon]] = js.native
  var getChatMessages: js.Function0[String] = js.native
  var sendChatMessage: js.Function1[js.UndefOr[String], Unit] = js.native

}
