package com.shocktrade.client.contest

import com.shocktrade.client.Filters.toDuration
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.ChatController._
import com.shocktrade.client.contest.DashboardController._
import com.shocktrade.client.users.UserService
import com.shocktrade.client.{GameStateService, GlobalLoading}
import com.shocktrade.common.models.contest.ChatMessage
import com.shocktrade.common.models.user.UserProfile
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.anchorscroll.AnchorScroll
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope, Timeout, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Chat Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class ChatController($scope: ChatControllerScope, $routeParams: DashboardRouteParams,
                          $anchorScroll: AnchorScroll, $cookies: Cookies, $timeout: Timeout, toaster: Toaster,
                          @injected("ContestService") contestService: ContestService,
                          @injected("GameStateService") gameStateService: GameStateService,
                          @injected("UserService") userService: UserService)
  extends Controller with GlobalLoading {

  implicit val cookies: Cookies = $cookies

  private val colorMap = js.Dictionary[String]()
  private var lastUpdateTime = 0d
  private var lastMessageCount = -1
  private var cachedHtml = ""

  $scope.chatMessage = ""
  $scope.chatMessages = js.Array()
  $scope.userProfile = js.undefined

  /////////////////////////////////////////////////////////////////
  //    Initialization Functions
  /////////////////////////////////////////////////////////////////

  $scope.initChat = () => $routeParams.contestID map initChat

  $scope.onMessagesUpdated { (_, _) => $scope.initChat() }

  $scope.onUserProfileUpdated { (_, _) => $scope.initChat() }

  private def initChat(contestID: String): js.Promise[HttpResponse[js.Array[ChatMessage]]] = {
    // attempt to load the user profile
    gameStateService.getUserID map { userID =>
      val outcome0 = userService.findUserByID(userID)
      outcome0 onComplete {
        case Success(userProfile) => $scope.$apply(() => $scope.userProfile = userProfile.data)
        case Failure(e) => console.error(s"Failed to retrieve user profile: ${e.getMessage}")
      }
      outcome0
    }

    // attempt to load the chat messages
    val outcome1 = contestService.findChatMessages(contestID)
    outcome1 onComplete {
      case Success(messages) => $scope.$apply(() => $scope.chatMessages = messages.data)
      case Failure(e) => console.error(s"Failed to retrieve chat messages: ${e.displayMessage}")
    }
    outcome1
  }

  /////////////////////////////////////////////////////////////////
  //    Scope Methods
  /////////////////////////////////////////////////////////////////

  $scope.addSmiley = (aEmoticon: js.UndefOr[Emoticon]) => aEmoticon.foreach(icon => $scope.chatMessage += " " + icon.symbol)

  $scope.getChatMessages = () => getChatMessages

  $scope.getEmoticons = () => Emoticons.reverse

  $scope.sendChatMessage = (aMessageText: js.UndefOr[String]) => {
    val outcome = aMessageText map sendChatMessage
    outcome.foreach(_ onComplete { _ => $anchorScroll() })
    outcome
  }

  private def colorOf(name: String): String = colorMap.getOrElseUpdate(name, Colors((1 + colorMap.size) % Colors.length))

  private def getChatMessages: String = {
    val chatMessages = $scope.chatMessages
    if ((chatMessages.length == lastMessageCount) && (js.Date.now() - lastUpdateTime) <= 2000) cachedHtml
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
          val outcome = for {
            _ <- contestService.putChatMessage(contestID, new ChatMessage(userID = userID, username = $scope.userProfile.flatMap(_.username), message = messageText))
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
          outcome.toJSPromise
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
trait ChatControllerScope extends Scope {
  // variables
  var chatMessage: String = js.native
  var chatMessages: js.Array[ChatMessage] = js.native
  var userProfile: js.UndefOr[UserProfile] = js.native

  // functions
  var initChat: js.Function0[js.UndefOr[js.Promise[HttpResponse[js.Array[ChatMessage]]]]] = js.native
  var addSmiley: js.Function1[js.UndefOr[Emoticon], Unit] = js.native
  var getEmoticons: js.Function0[js.Array[Emoticon]] = js.native
  var getChatMessages: js.Function0[String] = js.native
  var sendChatMessage: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[js.Array[ChatMessage]]]]] = js.native

}
