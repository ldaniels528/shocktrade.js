package com.shocktrade.client.contest

import com.shocktrade.client.Filters.toDuration
import com.shocktrade.client.contest.ChatController._
import com.shocktrade.client.contest.DashboardController._
import com.shocktrade.client.{GameStateService, RootScope}
import com.shocktrade.common.models.contest.ChatMessage
import io.scalajs.dom.html.HTMLElement
import io.scalajs.dom.html.browser.{Window, console}
import io.scalajs.jquery.JQuery.$
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Interval, Timeout}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Chat Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ChatController {
  self: Controller =>

  private var lastUpdateTime = 0d
  private var lastMessageCount = -1
  private var cachedHtml = ""

  def $scope: ChatControllerScope

  def $interval: Interval

  def $routeParams: DashboardRouteParams

  def $timeout: Timeout

  def $window: Window

  def contestService: ContestService

  def gameStateService: GameStateService

  def toaster: Toaster

  $scope.inputs = new ChatInputs(chatMessage = "")
  $scope.chatMessages = js.Array()

  /////////////////////////////////////////////////////////////////
  //    Scope Methods
  /////////////////////////////////////////////////////////////////

  $scope.addSmiley = (aEmoticon: js.UndefOr[Emoticon]) => aEmoticon.foreach(icon => $scope.inputs.chatMessage += " " + icon.symbol)

  $scope.clearMessageArea = () => $scope.inputs.chatMessage = ""

  $scope.getChatMessages = () => getChatMessages

  $scope.getEmoticons = () => Emoticons.reverse

  $scope.gotoBottom = () => {
    val chatBox = $("#chatBox")(0).asInstanceOf[HTMLTextAreaElement]
    if (chatBox != null) {
      console.log("chatBox => " + chatBox)
      console.log(s"chatBox: scrollTop => ${chatBox.scrollTop}, scrollHeight => ${chatBox.scrollHeight}")
      chatBox.scrollTop = chatBox.scrollHeight
      console.log("AFTER: scrollTop => " + chatBox.scrollTop)
    }
  }

  $scope.sendChatMessage = (aMessageText: js.UndefOr[String]) => aMessageText map sendChatMessage

  private def getChatMessages: String = {
    val chatMessages = $scope.chatMessages
    if ((chatMessages.length == lastMessageCount) && (js.Date.now() - lastUpdateTime) <= 2000) cachedHtml
    else {
      // capture the new number of lines
      lastMessageCount = chatMessages.length

      // build an HTML string with emoticons
      val viewHtml = chatMessages.foldLeft[String]("") { (html, msg) =>
        var text = new StringBuilder(msg.message getOrElse "")

        // replace the symbols with icon images
        if (text.nonEmpty) Emoticons.foreach { emo =>
          text = new StringBuilder(text.replaceAllLiterally(emo.symbol, s"""<img src="/images/smilies/${emo.uri}">"""))
        }

        // replace the user references (e.g. "@fugitive528")
        if (text.nonEmpty) {
          var offset: Int = -1
          do {
            offset = text.indexOf("@", offset)
            if (offset != -1) {
              var limit = offset + 1
              while (limit < text.length && text(limit).isLetterOrDigit) limit += 1
              val theUsernameWithTag = text.substring(offset, limit).trim
              val theUsername = text.substring(offset + 1, limit).trim
              val replacementText =
                s"""|<img src="/api/username/$theUsername/icon"
                    |     class="chat_icon"
                    |     title="$theUsername"><span class="chat_user_ref">$theUsernameWithTag</span>
                    |""".stripMargin
              text = text.replace(offset, limit, replacementText)
              offset = limit + (replacementText.length - theUsernameWithTag.length)
            }
          } while (offset != -1)
        }

        // build the message HTML
        val senderName = msg.username.orNull
        s"""|$html
            |<img src="/api/user/${msg.userID}/icon" class="chat_icon" title="$senderName">
            |<span class="st_bkg_color">[${toDuration(msg.creationTime)}]</span> $text<br>
            |""".stripMargin
      }

      //console.log(f"Generated HTML in ${js.Date.now() - startTime}%.1f msec(s)")
      cachedHtml = viewHtml
      lastUpdateTime = js.Date.now()
      cachedHtml
    }
  }

  /**
   * Sends a chat message to the server
   * @param messageText the given chat message text
   */
  private def sendChatMessage(messageText: String): js.Promise[HttpResponse[js.Array[ChatMessage]]] = {
    val result = for {
      userID <- gameStateService.getUserID.toOption
      contestID <- $routeParams.contestID.toOption
    } yield (userID, contestID)

    result match {
      case Some((userID, contestID)) =>
        if (messageText.trim.nonEmpty) {
          // make the service calls
          val outcome = (for {
            _ <- contestService.sendChatMessage(contestID, new ChatMessage(
              userID = userID,
              username = $scope.userProfile.flatMap(_.username),
              message = messageText
            ))
            messages <- contestService.findChatMessages(contestID)
          } yield messages).toJSPromise

          // transmit the message
          outcome onComplete {
            case Success(messages) =>
              $timeout(() => $scope.gotoBottom(), 1.millis)
              $scope.$apply { () =>
                $scope.clearMessageArea()
                $scope.chatMessages = messages.data
              }
            case Failure(e) =>
              toaster.error("Failed to send message")
              console.error(s"Failed to send message: ${e.displayMessage}")
          }
          outcome
        }
        else js.Promise.reject("No message text")

      case None =>
        toaster.error("No game selected")
        js.Promise.reject("No game selected")
    }
  }

}

/**
 * Chat Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ChatController {
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

  /**
   * Chat Controller Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait ChatControllerScope extends RootScope {
    // variables
    var inputs: ChatInputs = js.native
    var chatMessages: js.Array[ChatMessage] = js.native

    // functions
    var addSmiley: js.Function1[js.UndefOr[Emoticon], Unit] = js.native
    var clearMessageArea: js.Function0[Unit] = js.native
    var getEmoticons: js.Function0[js.Array[Emoticon]] = js.native
    var getChatMessages: js.Function0[String] = js.native
    var gotoBottom: js.Function0[Unit] = js.native
    var sendChatMessage: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[js.Array[ChatMessage]]]]] = js.native
  }

  class Emoticon(val symbol: String, val uri: String, val tooltip: String) extends js.Object

  class ChatInputs(var chatMessage: String = "") extends js.Object

  @js.native
  trait HTMLTextAreaElement extends HTMLElement {
    var scrollTop: Double = js.native
    var scrollHeight: Double = js.native
  }

}