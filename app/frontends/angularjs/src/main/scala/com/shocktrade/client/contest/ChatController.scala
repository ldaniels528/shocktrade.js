package com.shocktrade.client.contest

import com.shocktrade.client.Filters.toDuration
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.ChatController._
import com.shocktrade.client.users.GameStateFactory
import com.shocktrade.client.{GlobalLoading, RootScope}
import com.shocktrade.common.models.contest.ChatMessage
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.anchorscroll.AnchorScroll
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Timeout, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Chat Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ChatController($scope: ChatControllerScope, $anchorScroll: AnchorScroll, $routeParams: DashboardRouteParams,
                     $timeout: Timeout, toaster: Toaster,
                     @injected("ContestService") contestService: ContestService,
                     @injected("GameStateFactory") gameState: GameStateFactory)
  extends Controller with GlobalLoading {

  implicit private val scope: ChatControllerScope = $scope
  private val colorMap = js.Dictionary[String]()
  private var lastUpdateTime = 0d
  private var lastMessageCount = -1
  private var cachedHtml = ""

  $scope.chatMessage = ""
  $scope.chatMessages = js.Array()

  /////////////////////////////////////////////////////////////////
  //    Initialization Functions
  /////////////////////////////////////////////////////////////////

  $scope.initChat = () => $routeParams.contestID foreach initChat

  $scope.onUserProfileUpdated { (_, _) => $scope.initChat() }

  private def initChat(contestID: String): Unit = {
    contestService.findChatMessages(contestID) onComplete {
      case Success(messages) => $scope.$apply(() => $scope.chatMessages = messages.data)
      case Failure(e) =>
        toaster.error("Failed to retrieve chat messages")
        console.error(s"Failed to retrieve chat messages: ${e.displayMessage}")
    }
  }

  /////////////////////////////////////////////////////////////////
  //    Scope Methods
  /////////////////////////////////////////////////////////////////

  $scope.addSmiley = (aEmoticon: js.UndefOr[Emoticon]) => aEmoticon.foreach(icon => $scope.chatMessage += " " + icon.symbol)

  $scope.getChatMessages = () => getChatMessages

  $scope.getEmoticons = () => Emoticons

  $scope.sendChatMessage = (aMessageText: js.UndefOr[String]) => {
    aMessageText foreach sendChatMessage
    $anchorScroll()
    ()
  }

  private def getChatMessages: String = {
    val chatMessages = $scope.chatMessages
    if ((chatMessages.length == lastMessageCount) && (js.Date.now() - lastUpdateTime) <= 10000) cachedHtml
    else {
      // capture the new number of lines
      lastMessageCount = chatMessages.length

      // build an HTML string with emoticons
      val html = chatMessages.foldLeft[String]("") { (html, msg) =>
        // replace the symbols with icon images
        var text = msg.message getOrElse ""
        if (text.nonEmpty) {
          Emoticons.foreach { emo => text = text.replaceAllLiterally(emo.symbol, s"""<img src="/images/smilies/${emo.uri}">""") }
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

  /////////////////////////////////////////////////////////////////////////////
  //			Local Functions and Data
  /////////////////////////////////////////////////////////////////////////////

  private def colorOf(name: String): String = colorMap.getOrElseUpdate(name, Colors((1 + colorMap.size) % Colors.length))

  /**
   * Sends a chat message to the server
   * @param messageText the given chat message text
   */
  private def sendChatMessage(messageText: String): Unit = {
    val outcome = for {
      userID <- gameState.userID.toOption
      contestID <- $routeParams.contestID.toOption
    } yield (userID, contestID)

    outcome match {
      case Some((userID, contestID)) =>
        if (messageText.trim.nonEmpty) {
          // make the service calls
          val outcome = for {
            _ <- contestService.sendChatMessage(contestID, new ChatMessage(userID = userID, username = gameState.username, message = messageText))
            messages <- contestService.findChatMessages(contestID)
          } yield messages

          // transmit the message
          outcome onComplete {
            case Success(messages) =>
              $scope.$apply { () =>
                $scope.chatMessage = ""
                $scope.chatMessages = messages.data
              }
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
trait ChatControllerScope extends RootScope {
  // variables
  var chatMessage: String = js.native
  var chatMessages: js.Array[ChatMessage] = js.native

  // functions
  var initChat: js.Function0[Unit] = js.native
  var addSmiley: js.Function1[js.UndefOr[Emoticon], Unit] = js.native
  var getEmoticons: js.Function0[js.Array[Emoticon]] = js.native
  var getChatMessages: js.Function0[String] = js.native
  var sendChatMessage: js.Function1[js.UndefOr[String], Unit] = js.native

}
